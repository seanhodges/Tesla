package tesla.app.service.business;
import tesla.app.service.business.IErrorHandler;
import tesla.app.command.Command;

interface ICommandController {
	
	void registerErrorHandler(IErrorHandler cb);
	void unregisterErrorHandler(IErrorHandler cb);
	
	boolean connect();
	
	void sendCommand(out Command command);
	Command sendQuery(out Command command);
}