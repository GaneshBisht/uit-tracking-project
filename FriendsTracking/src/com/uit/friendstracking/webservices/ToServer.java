package com.uit.friendstracking.webservices;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import model.Photo;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.util.Base64;

import com.uit.friendstracking.models.KAuth;
import com.uit.friendstracking.models.KNote;
import com.uit.friendstracking.models.KPhoto;
import com.uit.friendstracking.models.KPosition;
import com.uit.friendstracking.models.KUserInfo;

public class ToServer {

	// Cache live 30 seconds.
	final static long cacheLive = 30000;

	// Session live 10 minutes.
	final static long sessionLive = 600000;

	// Last session update
	static long lastSessionUpdate;

	// The web service's URL.
	private static String URL;

	// The IP
	private static String ip;

	// The name space of the web service you want to access.
	private static final String NAMESPACE = "http://controller";

	// Not need to specify in our type of soap calls.
	private static final String SOAP_ACTION = "";
	private static KUserInfo myUser = null;

	private static String password;
	private static String nickname;

	// Notes cache system
	private static List<KNote> listNotes = null;
	private static long notesTime = System.currentTimeMillis();
	private static int selectedUserID = 0;

	// Friends cache system
	private static List<KUserInfo> listFriends = null;
	private static long friendsTime = System.currentTimeMillis();

	private static KAuth auth = null;

	public static void logout() {
		setAuth(null);
	}

	public static boolean logged() throws Exception {
		return getAuth() != null;
	}

	public static void setIP(String _ip) {
		if (_ip.length() > 0) {
			URL = "http://" + _ip + ":2720/fflServer/services/FFLocationAPI";
			ip = _ip;
		}
	}

	public static boolean login(String n, String p) throws Exception {

		URL = "http://" + ip + ":2720/fflServer/services/FFLocationAPI";
		try {
			password = p;
			nickname = n;
			p = getHash(p);

			lastSessionUpdate = System.currentTimeMillis();

			String METHOD_NAME = "login";
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

			request.addProperty("nick", n);
			request.addProperty("pw", p);

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL);

			envelope.addMapping("http://controller", "Auth", new KAuth().getClass());

			transport.call(SOAP_ACTION, envelope);

			setAuth((KAuth) envelope.getResponse());
			myUser = null;

		} catch (Exception ex) {
			return loginAlternative(n, p);
		}
		return getAuth() != null;
	}

	public static boolean loginAlternative(String n, String p) throws Exception {
		URL = "http://" + ip + ":2720/fflServer/services/FFLocationAPI?wsdl";

		password = p;
		nickname = n;
		p = getHash(p);

		lastSessionUpdate = System.currentTimeMillis();

		String METHOD_NAME = "login";
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

		request.addProperty("nick", n);
		request.addProperty("pw", p);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);

		HttpTransportSE transport = new HttpTransportSE(URL);

		envelope.addMapping("http://controller", "Auth", new KAuth().getClass());

		transport.call(SOAP_ACTION, envelope);

		setAuth((KAuth) envelope.getResponse());
		myUser = null;

		return getAuth() != null;

	}

	private static KAuth getAuth() throws Exception {
		long currentTime = System.currentTimeMillis();

		// If has been more time than sessionLive without update
		if ((currentTime - lastSessionUpdate) > sessionLive) {
			login(nickname, password);
		}
		lastSessionUpdate = currentTime;

		return auth;
	}

	private static void setAuth(KAuth au) {
		auth = au;

	}

	public static KUserInfo myUser() throws Exception {

		if (myUser == null) {

			if (getAuth() == null)
				throw new Exception("Not logged");
			KAuth a = getAuth();
			String METHOD_NAME = "myUser";
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

			request.addProperty("a", a);

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.setOutputSoapObject(request);
			envelope.addMapping("http://controller", "KAuth", KAuth.class);

			envelope.addMapping("http://model", "UserInfo", new KUserInfo().getClass());

			envelope.addMapping("http://model", "Position", new KPosition().getClass());

			HttpTransportSE transport = new HttpTransportSE(URL);
			transport.call(SOAP_ACTION, envelope);

			myUser = (KUserInfo) envelope.getResponse();

			return myUser;
		} else
			return myUser;
	}

	public static KNote getNote(int noteID) throws Exception {
		if (getAuth() == null)
			throw new Exception("Not logged");
		KAuth a = getAuth();
		String METHOD_NAME = "getNote";
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

		request.addProperty("a", a);
		request.addProperty("noteID", noteID);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);
		envelope.addMapping("http://controller", "KAuth", KAuth.class);

		envelope.addMapping("http://model", "Note", new KNote().getClass());

		envelope.addMapping("http://model", "Position", new KPosition().getClass());

		envelope.addMapping("http://model", "Photo", new KPhoto().getClass());

		HttpTransportSE transport = new HttpTransportSE(URL);
		transport.call(SOAP_ACTION, envelope);
		System.out.println(envelope.getResponse());
		return (KNote) envelope.getResponse();
	}

	public static List<KPosition> getPositions(int id, int c) throws Exception {
		if (getAuth() == null)
			throw new Exception("Not logged");
		KAuth a = getAuth();
		String METHOD_NAME = "getPositions";
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

		request.addProperty("a", a);
		request.addProperty("id", id);
		request.addProperty("c", c);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);
		envelope.addMapping("http://controller", "KAuth", KAuth.class);

		envelope.addMapping("http://model", "UserInfo", new KUserInfo().getClass());
		envelope.addMapping("http://model", "Position", new KPosition().getClass());

		HttpTransportSE transport = new HttpTransportSE(URL);
		transport.call(SOAP_ACTION, envelope);

		SoapObject so = (SoapObject) envelope.bodyIn;
		List<KPosition> us = new ArrayList<KPosition>();
		for (int i = 0; i < so.getPropertyCount(); i++)
			us.add((KPosition) so.getProperty(i));
		return us;
	}

	public static List<KUserInfo> getFriends() throws Exception {

		if (getAuth() == null)
			throw new Exception("Not logged");

		long currentTime = System.currentTimeMillis();
		if (listFriends == null || (currentTime - friendsTime) > cacheLive) {
			friendsTime = currentTime;
			KAuth a = getAuth();
			String METHOD_NAME = "getFriends";
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

			request.addProperty("a", a);

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.setOutputSoapObject(request);
			envelope.addMapping("http://controller", "KAuth", KAuth.class);

			envelope.addMapping("http://model", "UserInfo", new KUserInfo().getClass());

			envelope.addMapping("http://model", "Position", new KPosition().getClass());

			HttpTransportSE transport = new HttpTransportSE(URL);
			transport.call(SOAP_ACTION, envelope);

			SoapObject so = (SoapObject) envelope.bodyIn;
			listFriends = new ArrayList<KUserInfo>();
			for (int i = 0; i < so.getPropertyCount(); i++)
				listFriends.add((KUserInfo) so.getProperty(i));
			return listFriends;
		} else {
			return listFriends;
		}
	}

	public static boolean newUser(KUserInfo u, String pw) throws Exception {
		pw = getHash(pw);

		String METHOD_NAME = "newUser";
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

		request.addProperty("u", u);
		request.addProperty("pw", pw);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);

		envelope.addMapping("http://controller", "KUserInfo", KUserInfo.class);

		envelope.addMapping("http://controller", "KPosition", KPosition.class);

		HttpTransportSE transport = new HttpTransportSE(URL);

		envelope.addMapping("http://controller", "Auth", new KAuth().getClass());

		transport.call(SOAP_ACTION, envelope);

		return (envelope.getResponse() != null);
	}
	public static boolean newUser1(KUserInfo u, String pw, Photo photo) throws Exception {
		pw = getHash(pw);

		String METHOD_NAME = "newUser1";
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

		request.addProperty("u", u);
		request.addProperty("pw", pw);
		if (photo != null) {
			request.addProperty("photo", Base64.encodeToString(photo.getPhoto(), Base64.DEFAULT));
		} else {
			request.addProperty("photo", "null");
		}

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);

		envelope.addMapping("http://controller", "KUserInfo", KUserInfo.class);

		envelope.addMapping("http://controller", "KPosition", KPosition.class);

		HttpTransportSE transport = new HttpTransportSE(URL);

		envelope.addMapping("http://controller", "Auth", new KAuth().getClass());

		transport.call(SOAP_ACTION, envelope);

		return (envelope.getResponse() != null);
	}

	public static boolean changeUser(KUserInfo u, String pw) throws Exception {

		pw = getHash(pw);

		if (getAuth() == null)
			throw new Exception("Not logged");
		KAuth a = getAuth();
		String METHOD_NAME = "changeUser";

		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

		request.addProperty("a", a);
		request.addProperty("u", u);
		request.addProperty("pw", pw);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);

		envelope.addMapping("http://controller", "KAuth", KAuth.class);
		envelope.addMapping("http://controller", "KUserInfo", KUserInfo.class);

		envelope.addMapping("http://controller", "KPosition", KPosition.class);

		HttpTransportSE transport = new HttpTransportSE(URL);

		envelope.addMapping("http://controller", "Auth", new KAuth().getClass());

		transport.call(SOAP_ACTION, envelope);

		myUser = null;

		return Boolean.parseBoolean(envelope.getResponse().toString());

	}

	public static boolean logPosition(float longitude, float latitude) throws Exception {
		if (getAuth() == null)
			throw new Exception("Not logged");
		KAuth a = getAuth();
		String METHOD_NAME = "logPosition";
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

		KPosition p = new KPosition();
		p.setLongitude(longitude);
		p.setLatitude(latitude);
		request.addProperty("a", a);
		request.addProperty("p", p);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);

		envelope.addMapping("http://controller", "KAuth", KAuth.class);
		envelope.addMapping("http://controller", "KPosition", KPosition.class);

		HttpTransportSE transport = new HttpTransportSE(URL);
		transport.call(SOAP_ACTION, envelope);

		myUser = null;

		return Boolean.parseBoolean(envelope.getResponse().toString());
	}

	public static boolean askFriend(int to) throws Exception {
		if (getAuth() == null)
			throw new Exception("Not logged");
		KAuth a = getAuth();
		String METHOD_NAME = "askFriend";
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

		request.addProperty("a", a);
		request.addProperty("to", to);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);
		envelope.addMapping("http://controller", "KAuth", KAuth.class);

		HttpTransportSE transport = new HttpTransportSE(URL);
		transport.call(SOAP_ACTION, envelope);

		// Clear the listFriends
		listFriends = null;

		return Boolean.parseBoolean(envelope.getResponse().toString());
	}

	public static boolean exists(String nick) throws Exception {
		String METHOD_NAME = "exists";
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

		request.addProperty("nick", nick);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);

		HttpTransportSE transport = new HttpTransportSE(URL);

		transport.call(SOAP_ACTION, envelope);

		return Boolean.parseBoolean(envelope.getResponse().toString());
	}

	public static List<KUserInfo> searchFriend(String nick, String name, String surname, String country) throws Exception {
		if (getAuth() == null)
			throw new Exception("Not logged");
		KAuth a = getAuth();
		String METHOD_NAME = "searchFriend";
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

		request.addProperty("a", a);
		request.addProperty("nick", nick);
		request.addProperty("name", name);
		request.addProperty("surname", surname);
		request.addProperty("country", country);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);
		envelope.addMapping("http://controller", "KAuth", KAuth.class);

		envelope.addMapping("http://model", "UserInfo", new KUserInfo().getClass());

		HttpTransportSE transport = new HttpTransportSE(URL);
		transport.call(SOAP_ACTION, envelope);
		System.out.println(envelope.bodyIn.getClass());
		SoapObject so = (SoapObject) envelope.bodyIn;
		List<KUserInfo> us = new ArrayList<KUserInfo>();
		for (int i = 0; i < so.getPropertyCount(); i++)
			us.add((KUserInfo) so.getProperty(i));
		return us;
	}

	public static boolean setNote(float longitude, float latitude, String note, Photo photo) throws Exception {
		if (getAuth() == null)
			throw new Exception("Not logged");
		KAuth a = getAuth();
		String METHOD_NAME = "setNote";
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

		KPosition p = new KPosition();
		p.setLatitude(latitude);
		p.setLongitude(longitude);

		request.addProperty("a", a);
		request.addProperty("position", p);
		request.addProperty("note", note);
		if (photo != null) {
			request.addProperty("photo", Base64.encodeToString(photo.getPhoto(), Base64.DEFAULT));
		} else {
			request.addProperty("photo", "null");
		}

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);

		envelope.addMapping("http://controller", "KAuth", KAuth.class);
		envelope.addMapping("http://controller", "KPosition", KPosition.class);

		HttpTransportSE transport = new HttpTransportSE(URL);
		transport.call(SOAP_ACTION, envelope);

		return Boolean.parseBoolean(envelope.getResponse().toString());
	}

	public static List<KNote> getNotes(int id, int c, boolean attachPhotos) throws Exception {
		if (getAuth() == null)
			throw new Exception("Not logged");
		long currentTime = System.currentTimeMillis();

		if (listNotes == null || selectedUserID != id || (currentTime - notesTime) > cacheLive) {
			notesTime = currentTime;
			selectedUserID = id;
			KAuth a = getAuth();
			String METHOD_NAME = "getNotes";
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

			request.addProperty("a", a);
			request.addProperty("id", id);
			request.addProperty("c", c);
			request.addProperty("attachPhotos", attachPhotos);

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.setOutputSoapObject(request);
			envelope.addMapping("http://controller", "KAuth", KAuth.class);

			envelope.addMapping("http://model", "Note", new KNote().getClass());

			envelope.addMapping("http://model", "Position", new KPosition().getClass());

			envelope.addMapping("http://model", "Photo", new KPhoto().getClass());

			HttpTransportSE transport = new HttpTransportSE(URL);
			transport.call(SOAP_ACTION, envelope);
			SoapObject so = (SoapObject) envelope.bodyIn;
			listNotes = new ArrayList<KNote>();
			for (int i = 0; i < so.getPropertyCount(); i++)
				listNotes.add((KNote) so.getProperty(i));
		}

		return listNotes;
	}

	public static List<KUserInfo> getRequests() throws Exception {
		if (getAuth() == null)
			throw new Exception("Not logged");
		KAuth a = getAuth();
		String METHOD_NAME = "getRequests";
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

		request.addProperty("a", a);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);
		envelope.addMapping("http://controller", "KAuth", KAuth.class);

		envelope.addMapping("http://model", "UserInfo", new KUserInfo().getClass());

		envelope.addMapping("http://model", "Position", new KPosition().getClass());

		HttpTransportSE transport = new HttpTransportSE(URL);
		transport.call(SOAP_ACTION, envelope);

		SoapObject so = (SoapObject) envelope.bodyIn;
		List<KUserInfo> us = new ArrayList<KUserInfo>();
		for (int i = 0; i < so.getPropertyCount(); i++)
			us.add((KUserInfo) so.getProperty(i));
		return us;
	}

	private static String getHash(String message) throws NoSuchAlgorithmException {
		MessageDigest md;
		byte[] buffer, digest;
		String hash = "";

		buffer = message.getBytes();
		md = MessageDigest.getInstance("SHA1");
		md.update(buffer);
		digest = md.digest();

		for (byte aux : digest) {
			int b = aux & 0xff;
			if (Integer.toHexString(b).length() == 1)
				hash += "0";
			hash += Integer.toHexString(b);
		}
		return hash;
	}


	
}
