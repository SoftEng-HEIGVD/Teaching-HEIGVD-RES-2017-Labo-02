package ch.heigvd.res.labs.roulette.net.client;

import ch.heigvd.res.labs.roulette.data.*;
import ch.heigvd.res.labs.roulette.net.protocol.RouletteV1Protocol;
import ch.heigvd.res.labs.roulette.net.protocol.InfoCommandResponse;
import ch.heigvd.res.labs.roulette.net.protocol.RandomCommandResponse;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class implements the client side of the protocol specification (version 1).
 * 
 * @author Olivier Liechti
 * @authors Ludovic Richard, Luana Martelli
 */
public class RouletteV1ClientImpl implements IRouletteV1Client {

  /**
   * We used the decorator BufferedReader and PrintWriter like it was used in other files
   * InputStream reads the next byte and InputStreamReader is more specific, it reads next char
   * Finally, BufferedReader reads a string
   * For writing, we use PrintWriter because it's more powerful that BufferedWriter. It has method like println()
   * that is more specific than just write
   */

  private static final Logger LOG = Logger.getLogger(RouletteV1ClientImpl.class.getName());

  private Socket socket = new Socket();
  protected BufferedReader reader;
  protected PrintWriter writer;

  @Override
  public void connect(String server, int port) throws IOException {

    if (isConnected()) {
      return;
    }

    /* Connexion to a server with a specific port */
    socket = new Socket(server, port);
    reader = new BufferedReader( new InputStreamReader(socket.getInputStream(), "UTF-8"));
    writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

    /* We read the welcome message */
    reader.readLine();
  }

  @Override
  public void disconnect() throws IOException {
    if (socket == null || !isConnected()) {
      return;
    }

    writer.println(RouletteV1Protocol.CMD_BYE);
    writer.flush();

    /* Close connexion */
    reader.close();
    writer.close();
    socket.close();

    socket = null;
  }

  @Override
  public boolean isConnected() {
    return socket != null && socket.isConnected();
  }

  @Override
  public void loadStudent(String fullname) throws IOException {

    /* Sends the command */
    writer.println(RouletteV1Protocol.CMD_LOAD);
    writer.flush();
    /* Reads the answer */
    String response = reader.readLine();

    if (!response.equals(RouletteV1Protocol.RESPONSE_LOAD_START)) {
      System.out.println("Error while using" + RouletteV1Protocol.CMD_LOAD + "  command");
      return;
    }

    /* Writes the name of the student */
    writer.println(fullname);
    writer.flush();

    /* End of data */
    writer.println(RouletteV1Protocol.CMD_LOAD_ENDOFDATA_MARKER);
    writer.flush();

    /* Reads server's message */
    response = reader.readLine();

    if (!response.equals(RouletteV1Protocol.RESPONSE_LOAD_DONE)) {
      System.out.println("Error at the end of process ");
    }

  }

  @Override
  public void loadStudents(List<Student> students) throws IOException {

    /* Sends command */
    writer.println(RouletteV1Protocol.CMD_LOAD);
    writer.flush();

    /* Reads the answer */
    reader.readLine();

    /* Add students */
    for (Student s : students) {
      writer.println(s.getFullname());
      writer.flush();
    }

    /* End of command */
    writer.println(RouletteV1Protocol.CMD_LOAD_ENDOFDATA_MARKER);
    writer.flush();

    String response = reader.readLine();

    if (!response.equals(RouletteV1Protocol.RESPONSE_LOAD_DONE)) {
      System.out.println("Error at the end of process ");
    }
  }

  @Override
  public Student pickRandomStudent() throws EmptyStoreException, IOException {

    /* Sends command */
    writer.println(RouletteV1Protocol.CMD_RANDOM);
    writer.flush();

    /* We get the answer from the server */
    RandomCommandResponse info = JsonObjectMapper.parseJson(reader.readLine(), RandomCommandResponse.class);

    /* If the student has no name, then the list is empty */
    if (info.getFullname() == null) {
      throw new EmptyStoreException();
    }
    return new Student(info.getFullname());
  }

  @Override
  public int getNumberOfStudents() throws IOException {

    /* Sends command */
    writer.println(RouletteV1Protocol.CMD_INFO);
    writer.flush();

    /* Gets answer from the json class */
    String a = reader.readLine();
    InfoCommandResponse info = JsonObjectMapper.parseJson(a, InfoCommandResponse.class);

    return info.getNumberOfStudents();
  }

  @Override
  public String getProtocolVersion() throws IOException {

    /* Sends command */
    writer.println(RouletteV1Protocol.CMD_INFO);
    writer.flush();

    /* Gets answer from json class */
    InfoCommandResponse info = JsonObjectMapper.parseJson(reader.readLine(), InfoCommandResponse.class);
    return info.getProtocolVersion();
  }

  /* Usefull for the daughter's class */
  public void write(String towrite) {
    writer.println(towrite);
    writer.flush();
  }

  /* Usefull for the daughter's class */
  public String read() throws  IOException {
    return reader.readLine();
  }

}
