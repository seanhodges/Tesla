/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/sean/Documents/Android/Tesla/src/src/tesla/app/service/business/IErrorHandler.aidl
 */
package tesla.app.service.business;
import java.lang.String;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
public interface IErrorHandler extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements tesla.app.service.business.IErrorHandler
{
private static final java.lang.String DESCRIPTOR = "tesla.app.service.business.IErrorHandler";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an IErrorHandler interface,
 * generating a proxy if needed.
 */
public static tesla.app.service.business.IErrorHandler asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof tesla.app.service.business.IErrorHandler))) {
return ((tesla.app.service.business.IErrorHandler)iin);
}
return new tesla.app.service.business.IErrorHandler.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onServiceError:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
boolean _arg2;
_arg2 = (0!=data.readInt());
this.onServiceError(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements tesla.app.service.business.IErrorHandler
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public void onServiceError(java.lang.String title, java.lang.String message, boolean fatal) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(title);
_data.writeString(message);
_data.writeInt(((fatal)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_onServiceError, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onServiceError = (IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onServiceError(java.lang.String title, java.lang.String message, boolean fatal) throws android.os.RemoteException;
}
