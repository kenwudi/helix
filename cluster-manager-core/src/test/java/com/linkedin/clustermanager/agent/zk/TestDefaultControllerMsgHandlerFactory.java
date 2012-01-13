package com.linkedin.clustermanager.agent.zk;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.linkedin.clustermanager.ClusterManagerException;
import com.linkedin.clustermanager.NotificationContext;
import com.linkedin.clustermanager.agent.zk.DefaultControllerMessageHandlerFactory.DefaultControllerMessageHandler;
import com.linkedin.clustermanager.messaging.handling.MessageHandler;
import com.linkedin.clustermanager.model.Message;
import com.linkedin.clustermanager.model.Message.MessageType;

public class TestDefaultControllerMsgHandlerFactory
{
  @Test()
  public void testDefaultControllerMsgHandlerFactory()
  {
  	System.out.println("START TestDefaultControllerMsgHandlerFactory at " + new Date(System.currentTimeMillis()));

    DefaultControllerMessageHandlerFactory facotry = new DefaultControllerMessageHandlerFactory();

    Message message = new Message(MessageType.NO_OP, "0");
    NotificationContext context = new NotificationContext(null);

    boolean exceptionCaught = false;
    try
    {
      MessageHandler handler = facotry.createHandler(message, context);
    } catch (ClusterManagerException e)
    {
      exceptionCaught = true;
    }
    AssertJUnit.assertTrue(exceptionCaught);

    message = new Message(MessageType.CONTROLLER_MSG, "1");
    exceptionCaught = false;
    try
    {
      MessageHandler handler = facotry.createHandler(message, context);
    } catch (ClusterManagerException e)
    {
      exceptionCaught = true;
    }
    AssertJUnit.assertFalse(exceptionCaught);

    Map<String, String> resultMap = new HashMap<String, String>();
    message = new Message(MessageType.NO_OP, "3");
    DefaultControllerMessageHandler defaultHandler = new DefaultControllerMessageHandler(message, context);
    try
    {
      defaultHandler.handleMessage();
    } catch (ClusterManagerException e)
    {
      exceptionCaught = true;
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    AssertJUnit.assertTrue(exceptionCaught);

    message = new Message(MessageType.CONTROLLER_MSG, "4");
    defaultHandler = new DefaultControllerMessageHandler(message, context);
    exceptionCaught = false;
    try
    {
      defaultHandler.handleMessage();
    } catch (ClusterManagerException e)
    {
      exceptionCaught = true;
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    AssertJUnit.assertFalse(exceptionCaught);
    System.out.println("END TestDefaultControllerMsgHandlerFactory at " + new Date(System.currentTimeMillis()));
  }

}
