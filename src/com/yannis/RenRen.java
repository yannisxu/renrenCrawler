package com.yannis;

/** 
 * @author Yannis E-mail: excellentbright@gmail.com
 * @version ����ʱ�䣺2012-5-4 ����11:12:11 
 * ��˵�� 
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

public class RenRen {
	// �������˺�
	private String userName = "";
	// ����������
	private String password = "";

	private static String redirectURL = "http://www.renren.com/home";

	// Don't change the following URL
	private static String renRenLoginURL = "http://www.renren.com/PLogin.do";

	// The HttpClient is used in one session
	private HttpResponse response;
	private DefaultHttpClient httpclient = null;
	private HttpEntity httpEntity;
	private InputStream inputStream;
	private FileOutputStream fileOutputStream;

	private String redirectLocation = null;

	public RenRen(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	/**
	 * ��½
	 * 
	 * @return
	 */
	private boolean login() {
		if (httpclient != null) {
			System.out.println("�Ѿ���½�ɹ�");
			return true;
		}
		httpclient = null;
		httpclient = new DefaultHttpClient();
		HttpPost httpost = new HttpPost(renRenLoginURL);
		// All the parameters post to the web site
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("origURL", redirectURL));
		nvps.add(new BasicNameValuePair("domain", "renren.com"));
		nvps.add(new BasicNameValuePair("autoLogin", "true"));
		nvps.add(new BasicNameValuePair("formName", ""));
		nvps.add(new BasicNameValuePair("method", ""));
		nvps.add(new BasicNameValuePair("submit", "��¼"));
		nvps.add(new BasicNameValuePair("email", userName));
		nvps.add(new BasicNameValuePair("password", password));
		try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			response = httpclient.execute(httpost);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			httpost.abort();
		}
		System.out.println("login����  " + response.getStatusLine());
		redirectLocation = getRedirectLocation();
		if (getToken() == null) {
			System.out.println("��¼ʧ��");
		}
		return true;
	}

	private String getRedirectLocation() {
		Header locationHeader = response.getFirstHeader("Location");
		if (locationHeader == null) {
			return null;
		}
		return locationHeader.getValue();
	}

	private String getText(String redirectLocation) {
		HttpGet httpget = new HttpGet(redirectLocation);
		// Create a response handler
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = "";
		try {
			responseBody = httpclient.execute(httpget, responseHandler);
		} catch (Exception e) {
			e.printStackTrace();
			responseBody = null;
		} finally {
			httpget.abort();
		}

		return responseBody;
	}

	public String getToken() {
		String retu = getText(redirectLocation);
		String requesttoken = null;
		String temp = "requesttoken=";
		int start = retu.indexOf(temp);
		if (start != -1) {
			int end = retu.indexOf("\" class=\"logout\"");
			if (end != -1) {
				start += temp.length();
				String sss = retu.substring(start);
				requesttoken = sss.substring(0, end - start);
			}
		}
		System.out.println("��ȡ���û���requesttoken " + requesttoken);
		return requesttoken;
	}

	public String getRtk() {
		String retu = getText(redirectLocation);
		String rtk = null;
		String temp = "get_check_x:'";
		int start = retu.indexOf(temp);
		if (start != -1) {
			start += temp.length();
			int end = retu.indexOf("'", start + 1);
			if (end != -1) {
				String sss = retu.substring(start);
				rtk = sss.substring(0, end - start);
			}
		}
		System.out.println("��ȡ���û�-rtk��Ϣ " + rtk);
		return rtk;
	}

	public String getId() {
		String retu = getText(redirectLocation);
		String rtk = null;
		// user : {"id" : 259805366}
		String temp = "user : {\"id\" : ";
		int start = retu.indexOf(temp);
		if (start != -1) {
			start += temp.length();
			int end = retu.indexOf("}", start + 1);
			if (end != -1) {
				String sss = retu.substring(start);
				rtk = sss.substring(0, end - start);
			}
		}
		System.out.println("��ȡ���û�-ID��Ϣ " + rtk);
		return rtk;
	}

	/**
	 * �����û���״̬
	 * 
	 * @return
	 */

	private boolean update(String content) {
		if (login()) {
			String url = "http://shell.renren.com/" + getId() + "/status";
			HttpPost post = new HttpPost(url);

			List<NameValuePair> cp = new ArrayList<NameValuePair>();
			cp.add(new BasicNameValuePair("content", content));
			cp.add(new BasicNameValuePair("isAtHome", "1"));
			cp.add(new BasicNameValuePair("requestToken", getToken()));
			cp.add(new BasicNameValuePair("channel", "renren"));
			cp.add(new BasicNameValuePair("_rtk", getRtk()));

			try {
				post.setEntity(new UrlEncodedFormEntity(cp, HTTP.UTF_8));
				response = httpclient.execute(post);

				System.out.println("����״̬���� " + response.getStatusLine());

			} catch (Exception e) {
				e.printStackTrace();
				return false;
			} finally {
				post.abort();
			}
			return true;
		}
		return false;
	}

	/**
	 * ����
	 * 
	 * @param id
	 * @return
	 */

	private void visit(String id) {
		// ע�����������get������post����,http://www.renren.com/profile.do?portal=homeFootprint&ref=home_footprint&id=253658998
		HttpGet get = new HttpGet(
				"http://www.renren.com/profile.do?portal=homeFootprint&ref=home_footprint&id="
						+ id);
		
		try {
			response = httpclient.execute(get);
			httpEntity = response.getEntity();
			inputStream = httpEntity.getContent();
			int index = 0;
			String content = "";
			byte bytes[] = new byte[1024*1000];
			fileOutputStream = new FileOutputStream("data/"+id + ".html");
			int count = inputStream.read(bytes, index, 1024*100);
			while (count != -1) {
				index += count;
				count = inputStream.read(bytes, index, 1024*100);
			}
			fileOutputStream.write(bytes, 0, index);
			//System.out.println("index ��" + index);
			System.out.println(content);
//			while (true) {
//				byte[] bytes = new byte[1024*1000];
//				int k = inputStream.read(bytes, index, 1024*100);
//				if (k >=0 ) {
//					content = content + new String(bytes, 0, k);
//				}
//				else {
//					break;
//				}
//			}
//			System.out.println(content);
//			System.out.println("======================================================================");
//			fileOutputStream.write(inputStream.read());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("�ι۱�����ҳid " + id + " ����  "
				+ response.getStatusLine());
		// System.out.println("���ʳɹ���id�ţ�" + id);
		get.abort();
	}

	/**
	 * ���������� û��ʵ��
	 * 
	 * @param id
	 *            ����ID
	 * @return
	 */

	private boolean message(String id) {

		return true;
	}
	
	/**
	 * ��ȡ�û�
	 * 
	 * @param id
	 *            ����ID
	 * @return
	 */
	
	private void getInfo(String id){
		HttpGet get = new HttpGet(
				"http://www.renren.com/" + id + "?v=info_ajax&undefined");
		
		try {
			response = httpclient.execute(get);
			httpEntity = response.getEntity();
			inputStream = httpEntity.getContent();
			int index = 0;
			String content = "";
			byte bytes[] = new byte[1024*1000];
			fileOutputStream = new FileOutputStream("info/"+id + ".html");
			int count = inputStream.read(bytes, index, 1024*100);
			while (count != -1) {
				index += count;
				count = inputStream.read(bytes, index, 1024*100);
			}
			fileOutputStream.write(bytes, 0, index);
			System.out.println("index ��" + index);
			System.out.println(content);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("����idΪ" + id + " ����Ϣ�ɹ� "
				+ response.getStatusLine());
		get.abort();
	}
	
	private void getContract(String id) {
		HttpGet get = new HttpGet(
				"http://friend.renren.com/getprofilecontact/" + id);
		
		try {
			response = httpclient.execute(get);
			httpEntity = response.getEntity();
			inputStream = httpEntity.getContent();
			int index = 0;
			String content = "";
			byte bytes[] = new byte[1024*1000];
			fileOutputStream = new FileOutputStream("contract/"+id + ".html");
			int count = inputStream.read(bytes, index, 1024*100);
			while (count != -1) {
				index += count;
				count = inputStream.read(bytes, index, 1024*100);
			}
			fileOutputStream.write(bytes, 0, index);
			System.out.println("index ��" + index);
			System.out.println(content);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("����idΪ" + id + " ����ϵ��ʽ�ɹ� "
				+ response.getStatusLine());
		get.abort();
	}

	public void getStatus(String id) {
		int page;
		for (page = 0; page < 5; page++) {
			HttpGet get = new HttpGet(
					"http://status.renren.com/GetSomeomeDoingList.do?userId=" + id + "&curpage=" + page);
			
			try {
				response = httpclient.execute(get);
				httpEntity = response.getEntity();
				inputStream = httpEntity.getContent();
				int index = 0;
				String content = "";
				byte bytes[] = new byte[1024*1000];
				fileOutputStream = new FileOutputStream("status/"+id + "page=" + page +".html");
				int count = inputStream.read(bytes, index, 1024*100);
				while (count != -1) {
					index += count;
					count = inputStream.read(bytes, index, 1024*100);
				}
				fileOutputStream.write(bytes, 0, index);
				System.out.println("index ��" + index);
				System.out.println(content);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("��ȡidΪ" + id + " ��״̬ "
					+ response.getStatusLine());
			get.abort();
		}
		
	}
	
	/**
	 * ȡ�������еĺ���ID
	 * 
	 * @return
	 */

	private List<String> getFriends() {
		List<String> friends = new LinkedList<String>();

		int id = 0;
		// http://friend.renren.com/myfriendlistx.do#item_0
		// ע�����������get������post����,http://www.renren.com/profile.do?portal=homeFootprint&ref=home_footprint&id=253658998
		HttpGet get = new HttpGet("http://friend.renren.com/myfriendlistx.do");
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = "";
		try {
			responseBody = httpclient.execute(get, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			responseBody = null;
		} catch (IOException e) {
			e.printStackTrace();
			responseBody = null;
		} finally {
			get.abort();
		}
		int start = responseBody.indexOf("var friends");
		responseBody = responseBody.substring(start);
		int end = responseBody.indexOf("hotfriends");
		responseBody = responseBody.substring(0, end);
		// "id":253658998,"vip"
		String reg = "\"id\":\\d+,\"vip\"";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(responseBody);
		while (matcher.find()) {
			// �õ����ƥ�����е�����
			int groupCount = matcher.groupCount();
			// �õ�ÿ����������
			for (int i = 0; i <= groupCount; i++) {
				String temp = matcher.group(i);
				System.out.println(temp);
				reg = "\"id\":";
				start = temp.indexOf(reg);
				start += reg.length();
				end = temp.indexOf(",", start);
				System.out.println(start + " " + end);
				temp = temp.substring(start, end);
				friends.add(temp);
			}
		}
		System.out.println("������Ŀ  " + friends.size());
		System.out.println("��ȡ�������ѷ��� " + response.getStatusLine());

		return friends;
	}

	
	
	public void logout() {
		if (login()) {
			String retu = getText(redirectLocation);
			String logoutUrl = "http://www.renren.com/Logout.do";
			HttpPost httpost = new HttpPost(logoutUrl);
			// All the parameters post to the web site
			List<NameValuePair> nvps2 = new ArrayList<NameValuePair>();
			nvps2.add(new BasicNameValuePair("requesttoken", getToken()));
			try {
				httpost.setEntity(new UrlEncodedFormEntity(nvps2, HTTP.UTF_8));
				response = httpclient.execute(httpost);
				System.out.println("�˳�״̬ " + response.getStatusLine());
				httpost.abort();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				httpost.abort();
				httpclient.getConnectionManager().shutdown();
			}
		}
	}

	/**
	 * main����
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		RenRen ren = new RenRen("ff800u@yahoo.com.cn", "hadoop123");
		ren.login();
		// �����û�״̬
		// ren.update("�������״̬����Ϣ");
		List<String> friends = ren.getFriends();
		Iterator<String> it = friends.iterator();
		String idget = "";
		while (it.hasNext()) {
			try {
				idget = it.next(); 
				ren.visit(idget);
				ren.getInfo(idget);
				ren.getContract(idget);
				ren.getStatus(idget);
				
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println(it.hasNext());
				System.out.println("error");
			}
		}
		ren.logout();
	}

}
