package ch.heigvd.res.labs.roulette.net.server;

import ch.heigvd.res.labs.roulette.data.EmptyStoreException;
import ch.heigvd.res.labs.roulette.data.IStudentsStore;
import ch.heigvd.res.labs.roulette.data.JsonObjectMapper;
import ch.heigvd.res.labs.roulette.data.StudentsList;
import ch.heigvd.res.labs.roulette.net.protocol.*;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the Roulette protocol (version 2).
 *
 * @author Olivier Liechti
 */
public class RouletteV2ClientHandler implements IClientHandler
{

   final static Logger LOG = Logger.getLogger(RouletteV2ClientHandler.class.getName());

   private final IStudentsStore store;
   private int numberOfCommands = 0;

   public RouletteV2ClientHandler(IStudentsStore store)
   {
      this.store = store;
   }

   @Override
   public void handleClientConnection(InputStream is, OutputStream os) throws IOException
   {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(os));

      writer.println("Hello. Online HELP is available. Will you find it?");
      writer.flush();

      String command;
      boolean done = false;
      while (!done && ((command = reader.readLine()) != null))
      {
         numberOfCommands++;

         LOG.log(Level.INFO, "COMMAND: {0}", command);
         switch (command.toUpperCase())
         {
            case RouletteV1Protocol.CMD_RANDOM:
               RandomCommandResponse rcResponse = new RandomCommandResponse();
               try
               {
                  rcResponse.setFullname(store.pickRandomStudent().getFullname());
               } catch (EmptyStoreException ex)
               {
                  rcResponse.setError("There is no student, you cannot pick a random one");
               }
               writer.println(JsonObjectMapper.toJson(rcResponse));
               writer.flush();
               break;

            case RouletteV1Protocol.CMD_HELP:
               writer.println("Commands: " + Arrays.toString(RouletteV1Protocol.SUPPORTED_COMMANDS));
               break;

            case RouletteV1Protocol.CMD_INFO:
               InfoCommandResponse response = new InfoCommandResponse(RouletteV2Protocol.VERSION, store.getNumberOfStudents());
               writer.println(JsonObjectMapper.toJson(response));
               writer.flush();
               break;

            case RouletteV1Protocol.CMD_LOAD: //TODO relecture, semble marcher
               writer.println(RouletteV1Protocol.RESPONSE_LOAD_START);
               writer.flush();
               store.importData(reader);
               LoadCommandResponse reply = new LoadCommandResponse("success", store.getNumberOfStudents());
               writer.println(JsonObjectMapper.toJson(reply));
               writer.flush();
               break;

            case RouletteV1Protocol.CMD_BYE: //TODO relecture, semble marcher
               ByeCommandResponse bye = new ByeCommandResponse("success", numberOfCommands);
               writer.println(JsonObjectMapper.toJson(bye));
               writer.flush();
               done = true;
               break;

            case RouletteV2Protocol.CMD_LIST: //TODO relecture, semble marcher
               StudentsList list = new StudentsList();
               list.addAll(store.listStudents());

               writer.println(JsonObjectMapper.toJson(list));
               writer.flush();
               break;

            case RouletteV2Protocol.CMD_CLEAR: //TODO relecture, semble marcher
               store.clear();
               writer.println(RouletteV2Protocol.RESPONSE_CLEAR_DONE);
               writer.flush();
               break;

            default:
               writer.println("Huh? please use HELP if you don't know what commands are available.");
               writer.flush();
               break;
         }
         writer.flush();
      }
   }
}
