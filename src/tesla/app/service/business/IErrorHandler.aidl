package tesla.app.service.business;

interface IErrorHandler {
	
	void onServiceError(String title, String message, boolean fatal);
	
}