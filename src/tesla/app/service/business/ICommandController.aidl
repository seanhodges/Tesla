package tesla.app.service.business;
import tesla.app.service.business.IErrorHandler;

interface ICommandController {
	
	void registerErrorHandler(IErrorHandler cb);
	void unregisterErrorHandler(IErrorHandler cb);
	
	void sendCommand(out Command command);
	Command sendQuery(out Command command);
}