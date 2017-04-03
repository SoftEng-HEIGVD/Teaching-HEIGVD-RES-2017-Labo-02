package ch.heigvd.res.labs.roulette.net.client;

import ch.heigvd.res.labs.roulette.data.EmptyStoreException;
import ch.heigvd.res.labs.roulette.data.JsonObjectMapper;
import ch.heigvd.res.labs.roulette.net.protocol.RouletteV1Protocol;
import ch.heigvd.res.labs.roulette.data.Student;
import ch.heigvd.res.labs.roulette.net.protocol.InfoCommandResponse;
import ch.heigvd.res.labs.roulette.net.protocol.RandomCommandResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the client side of the protocol specification (version 1).
 *
 * @author Olivier Liechti
 */
public class RouletteV1ClientImpl implements IRouletteV1Client {

    private static final Logger LOG = Logger.getLogger(RouletteV1ClientImpl.class.getName());

    Socket socket;
    boolean connected = false;
    BufferedReader in;
    PrintWriter out;

    @Override
    public void connect(String server, int port) throws IOException {
        try {
            socket = new Socket(server, port);
            connected = true;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Unable to connect to server: {0}", e.getMessage());
            return;
        }
        in.readLine();
        out.flush();
    }

    @Override
    public void disconnect() throws IOException {
        LOG.log(Level.INFO, "client has requested to be disconnected.");
        if(connected == false){
            return;
        }
        connected = false;
        out.println(RouletteV1Protocol.CMD_BYE);
        out.flush();
        
        //close input output & socket
        out.close();
        in.close();
        socket.close();
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void loadStudent(String fullname) throws IOException {
        //send load command, student, then endofdata
        out.println(RouletteV1Protocol.CMD_LOAD);
        out.flush();
        in.readLine();
        out.println(fullname);
        out.flush();
        out.println(RouletteV1Protocol.CMD_LOAD_ENDOFDATA_MARKER);
        out.flush();
        in.readLine();
    }

    @Override
    public void loadStudents(List<Student> students) throws IOException {
        out.println(RouletteV1Protocol.CMD_LOAD);
        out.flush();
        in.readLine();
        for (Student student : students) {
            out.println(student.getFullname());
            out.flush();
        }
        out.println(RouletteV1Protocol.CMD_LOAD_ENDOFDATA_MARKER);
        out.flush();
        in.readLine();
    }

    @Override
    public Student pickRandomStudent() throws EmptyStoreException, IOException {
        out.println(RouletteV1Protocol.CMD_RANDOM);
        out.flush();
        //receive response
        RandomCommandResponse response = JsonObjectMapper.parseJson(in.readLine(),RandomCommandResponse.class);
        //check if it is an error, and if so throw an exception. Otherwise, return a new student
        if(response.getError() != null){
            throw new EmptyStoreException();
        }
        return new Student(response.getFullname());
        
    }

    @Override
    public int getNumberOfStudents() throws IOException {
        out.println(RouletteV1Protocol.CMD_INFO);
        out.flush();
        //parse the JSON response as a number of students
        InfoCommandResponse response = JsonObjectMapper.parseJson(in.readLine(), InfoCommandResponse.class);
        return response.getNumberOfStudents();
    }

    @Override
    public String getProtocolVersion() throws IOException {
        out.println(RouletteV1Protocol.CMD_INFO);
        out.flush();
        //parse the JSON response as a protocol version
        InfoCommandResponse response = JsonObjectMapper.parseJson(in.readLine(), InfoCommandResponse.class);
        return response.getProtocolVersion();
    }


}
