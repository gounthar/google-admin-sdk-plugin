package org.jenkinsci.plugins.googleadmin

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.admin.directory.Directory
import com.google.api.services.admin.directory.DirectoryScopes


def SCOPES = new ArrayList<String>()
SCOPES.add(DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY)
SCOPES.add(DirectoryScopes.ADMIN_DIRECTORY_GROUP_READONLY)
String APPLICATION_NAME = System.getenv("GOOGLE_ADMIN_APPLICATION_NAME") ?: "Jenkins Post-Init Script"
String CREDENTIALS_FOLDER = System.getenv("GOOGLE_ADMIN_CREDENTIALS_FOLDER") ?: "credentials"
String CLIENT_SECRET_FILE = System.getenv("GOOGLE_ADMIN_CLIENT_SECRET_FILE") ?: "client_secret.json"

def HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance()
def dataStoreFactory = new FileDataStoreFactory(new File(CREDENTIALS_FOLDER))
def secretsFileStream = new FileInputStream(CLIENT_SECRET_FILE)

def clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(secretsFileStream))

def flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(dataStoreFactory)
        .setApprovalPrompt("force")
        .setAccessType("offline")
        .build();

def credentials = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user")
def service = new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, flow)
        .setApplicationName(APPLICATION_NAME)
        .build()

final groupMembers = new GoogleAdminService()

