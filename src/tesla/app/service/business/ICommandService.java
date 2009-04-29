/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/sean/Documents/Android/Tesla/src/src/tesla/app/service/business/ICommandService.aidl
 */
package tesla.app.service.business;
import java.lang.String;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
import tesla.app.command.Command;
public interface ICommandService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements tesla.app.service.business.ICommandService
{
private static final java.lang.String DESCRIPTOR = "tesla.app.service.business.ICommandService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an ICommandService interface,
 * generating a proxy if needed.
 */
public static tesla.app.service.business.ICommandService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof tesla.app.service.business.ICommandService))) {
return ((tesla.app.service.business.ICommandService)iin);
}
return new tesla.app.service.business.ICommandService.Stub.Proxy(obj);
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
case TRANSACTION_sendCommand:
{
data.enforceInterface(DESCRIPTOR);
tesla.app.command.Command _arg0;
_arg0 = new tesla.app.command.Command();
this.sendCommand(_arg0);
reply.writeNoException();
if ((_arg0!=null)) {
reply.writeInt(1);
_arg0.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_sendQuery:
{
data.enforceInterface(DESCRIPTOR);
tesla.app.command.Command _arg0;
_arg0 = new tesla.app.command.Command();
tesla.app.command.Command _result = this.sendQuery(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
if ((_arg0!=null)) {
reply.writeInt(1);
_arg0.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements tesla.app.service.business.ICommandService
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
public void sendCommand(tesla.app.command.Command command) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_sendCommand, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
command.readFromParcel(_reply);
}
}
finally {
_reply.recycle();
_data.recycle();
}
}
public tesla.app.command.Command sendQuery(tesla.app.command.Command command) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
tesla.app.command.Command _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_sendQuery, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = tesla.app.command.Command.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
if ((0!=_reply.readInt())) {
command.readFromParcel(_reply);
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_sendCommand = (IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_sendQuery = (IBinder.FIRST_CALL_TRANSACTION + 1);
}
public void sendCommand(tesla.app.command.Command command) throws android.os.RemoteException;
public tesla.app.command.Command sendQuery(tesla.app.command.Command command) throws android.os.RemoteException;
}
