package tesla.app.service.business;

interface ICommandService {
	
	void sendCommand(out Command command);
	
	Command sendQuery(out Command command);
}