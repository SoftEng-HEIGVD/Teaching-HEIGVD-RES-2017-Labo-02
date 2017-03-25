package ch.heigvd.res.labs.roulette.net.protocol;

/**
 * Created by matthieu on 3/25/17.
 */
public class ByeCommandResponse
{
   private String status;
   private int numberOfCommands;

   public ByeCommandResponse(String status, int numberOfCommands)
   {
      this.status = status;
      this.numberOfCommands = numberOfCommands;
   }

   public String getStatus()
   {
      return status;
   }

   public void setStatus(String status)
   {
      this.status = status;
   }

   public int getNumberOfCommands()
   {
      return numberOfCommands;
   }

   public void setNumberOfCommands(int numberOfCommands)
   {
      this.numberOfCommands = numberOfCommands;
   }
}
