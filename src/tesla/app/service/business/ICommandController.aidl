package tesla.app.service.business;
import tesla.app.service.business.IErrorHandler;
import tesla.app.command.Command;

interface ICommandController {
	
	void registerErrorHandler(IErrorHandler cb);
	void unregisterErrorHandler(IErrorHandler cb);
	
	boolean connect();
	
	Command queryForCommand(in String key);
	void sendCommand(in Command command);
	Command sendQuery(in Command command);
}