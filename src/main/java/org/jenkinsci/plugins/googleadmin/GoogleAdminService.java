package org.jenkinsci.plugins.googleadmin;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.Members;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GoogleAdminService {

  private final String applicationName = "Google Admin Service @ Jenkins";

  private final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

  private final Directory service;

  private final List<String> scopes = Arrays.asList(
      DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY,
      DirectoryScopes.ADMIN_DIRECTORY_GROUP_READONLY
  );

  GoogleAdminService(String credentialsFolder, String clientSecretFile, String adminAccountEmail) throws GeneralSecurityException, IOException {

    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    InputStream in = new FileInputStream(clientSecretFile);
    GoogleCredential credential = GoogleCredential.fromStream(in, httpTransport, jsonFactory).createScoped(scopes);
    credential = new GoogleCredential.Builder()
            .setTransport(httpTransport)
            .setJsonFactory(jsonFactory)
            .setServiceAccountId(credential.getServiceAccountId())
            .setServiceAccountScopes(credential.getServiceAccountScopes())
            .setServiceAccountPrivateKey(credential.getServiceAccountPrivateKey())
            .setServiceAccountUser(adminAccountEmail)
            .build();

    service = new Directory.Builder(httpTransport, jsonFactory, null)
            .setApplicationName(applicationName)
            .setHttpRequestInitializer(credential).build();

  }

  public List<String> getGroupMembers(String groupKey) throws IOException {
    Members members = service.members().list(groupKey).execute();
    if (members == null || members.size() == 0) {
        return new ArrayList<>();
    } else {
      List<String> emails = new ArrayList<>();
      for (Member member : members.getMembers()) {
        emails.add(member.getEmail());
      }
      return emails;
    }
  }

//  public static void main(String[] args) {
//    try {
//      List<String> members = new GoogleAdminService(".credentials/", ".credentials/credentials.json", "admin@test.com").getGroupMembers("group@test.com");
//      System.out.println(members);
//    } catch (Exception e) {
//      System.out.println("error");
//      e.printStackTrace();
//    }
//  }
}
