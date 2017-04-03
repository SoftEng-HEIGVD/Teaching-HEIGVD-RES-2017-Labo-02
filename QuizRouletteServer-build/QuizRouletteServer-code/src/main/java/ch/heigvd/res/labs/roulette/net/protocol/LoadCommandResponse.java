package ch.heigvd.res.labs.roulette.net.protocol;

/**
 * This class is used to serialize/deserialize the response sent by the server
 * when processing the "LOAD" command defined in the protocol specification. The
 * JsonObjectMapper utility class can use this class.
 *
 * @author Kevin Moreira
 */
public class LoadCommandResponse
{

    private String status;
    private int nbrNewStudents;

    public LoadCommandResponse()
    {
        this.nbrNewStudents = 0;
    }

    public LoadCommandResponse(String status, int nbrNewStudents)
    {
        this.nbrNewStudents = nbrNewStudents;
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public int getNumberOfNewStudents()
    {
        return nbrNewStudents;
    }

    public void setNumberOfNewStudents(int nbrNewStudents)
    {
        this.nbrNewStudents = nbrNewStudents;
    }

}