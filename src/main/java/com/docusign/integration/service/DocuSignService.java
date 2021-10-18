package com.docusign.integration.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.Configuration;
import com.docusign.esign.client.auth.OAuth;
import com.docusign.esign.client.auth.OAuth.UserInfo;
import com.docusign.esign.model.Document;
import com.docusign.esign.model.EnvelopeDefinition;
import com.docusign.esign.model.EnvelopeSummary;
import com.docusign.esign.model.RecipientViewRequest;
import com.docusign.esign.model.Recipients;
import com.docusign.esign.model.Signer;
import com.docusign.esign.model.ViewUrl;
import com.docusign.integration.dao.Receipient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.migcomponents.migbase64.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocuSignService {

    @Autowired
    ResourceLoader resourceLoader;

    @Value("${docusign.server.url}")
    String docusignurl;

    @Value("${docusign.server.integrationkey}")
    String integrationkey;

    @Value("${docusign.server.username}")
    String username;

    @Value("${docusign.server.returnurl}")
    String returnurl;
    
    public Map<String,Object> createEmbeddedSending(List<Receipient> receipients, List<MultipartFile> file){
        Map<String,Object> map = null;
        try{
            map = new HashMap<>();
            Resource resource = resourceLoader.getResource("classpath:static/privatekey.txt");
            File privateKeyFile = resource.getFile();
            FileInputStream fin = new FileInputStream(privateKeyFile);
            byte fileContent[] = new byte[(int)privateKeyFile.length()];
            fin.read(fileContent);
            fin.close();
            List<Document> docs = new ArrayList<Document>();
            Integer docId = 0;
            List<Map<String,String>> docMaps = new ArrayList<>();
            Map<String,String> docMap;
            for(MultipartFile signdoc : file){
                docId++;
                docMap = new HashMap<>();
                docMap.put("docName", signdoc.getName());
                docMap.put("docOriginalName", signdoc.getOriginalFilename());
                docMap.put("docId",String.valueOf(docId));
                docMaps.add(docMap);
                // add a document to the envelope
                Document doc = new Document();
                String base64Doc = Base64.encodeToString(signdoc.getBytes(), false);
                doc.setDocumentBase64(base64Doc);
                doc.setName(signdoc.getName());
                doc.setDocumentId(String.valueOf(docId));
                doc.setFileExtension(StringUtils.getFilenameExtension(signdoc.getOriginalFilename()));
                docs.add(doc);
            }
            // create an envelope to be signed
            EnvelopeDefinition envDef = new EnvelopeDefinition();
            envDef.setEmailSubject("Embedded Signer");
		    envDef.setEmailBlurb("Embedded Signer");
            envDef.setDocuments(docs);
            List<Signer> signers = new ArrayList<>();
            Integer recpId = 0;
            for(Receipient receipient: receipients){
                recpId++;
                // Add a recipient to sign the document
                Signer signer = new Signer();
                signer.setEmail(receipient.getEmailId());
                signer.setName(receipient.getName());
                signer.setRecipientId(String.valueOf(recpId));
                signer.setClientUserId(signer.getEmail());
                signers.add(signer);
            }
            // Above causes issue
            envDef.setRecipients(new Recipients());
            envDef.getRecipients().setSigners(signers);

            // send the envelope (otherwise it will be "created" in the Draft folder
            envDef.setStatus("created");

            ApiClient apiClient = new ApiClient(docusignurl);

            // IMPORTANT NOTE:
			// the first time you ask for a JWT access token, you should grant access by making the following call
			// get DocuSign OAuth authorization url:
			//String oauthLoginUrl = apiClient.getJWTUri(IntegratorKey, RedirectURI, OAuthBaseUrl);
			// open DocuSign OAuth authorization url in the browser, login and grant access
			//Desktop.getDesktop().browse(URI.create(oauthLoginUrl));
			// END OF NOTE

			java.util.List<String> scopes = new ArrayList<String>();
			scopes.add(OAuth.Scope_SIGNATURE);
            scopes.add(OAuth.Scope_IMPERSONATION);

			OAuth.OAuthToken oAuthToken = apiClient.requestJWTUserToken(integrationkey, username, scopes, fileContent, 3600);
			// now that the API client has an OAuth token, let's use it in all
			// DocuSign APIs
			apiClient.setAccessToken(oAuthToken.getAccessToken(), oAuthToken.getExpiresIn());
			UserInfo userInfo = apiClient.getUserInfo(oAuthToken.getAccessToken());

			// parse first account's baseUrl
			// below code required for production, no effect in demo (same
			// domain)
			apiClient.setBasePath(userInfo.getAccounts().get(0).getBaseUri() + "/restapi");
			Configuration.setDefaultApiClient(apiClient);
			String accountId = userInfo.getAccounts().get(0).getAccountId();

			EnvelopesApi envelopesApi = new EnvelopesApi();
			EnvelopeSummary envelopeSummary = envelopesApi.createEnvelope(accountId, envDef);

			ViewUrl viewUrl = envelopesApi.createSenderView(accountId, envelopeSummary.getEnvelopeId(), null);
            map.put("envelopeId", envelopeSummary.getEnvelopeId());
            map.put("viewUrl", viewUrl.getUrl());
            map.put("documents", docMaps);
        }catch(Exception e){
            map = null;
            e.printStackTrace();
        }
        return map;
    }

    public Map<String,String> createEmbeddedSignning(String envelopeId, Receipient receipient){
        Map<String,String> map = null;
        try{
            map = new HashMap<>();
            Resource resource = resourceLoader.getResource("classpath:static/privatekey.txt");
            File privateKeyFile = resource.getFile();
            FileInputStream fin = new FileInputStream(privateKeyFile);
            byte fileContent[] = new byte[(int)privateKeyFile.length()];
            fin.read(fileContent);
            fin.close();
            ApiClient apiClient = new ApiClient(docusignurl);

            // IMPORTANT NOTE:
			// the first time you ask for a JWT access token, you should grant access by making the following call
			// get DocuSign OAuth authorization url:
			//String oauthLoginUrl = apiClient.getJWTUri(IntegratorKey, RedirectURI, OAuthBaseUrl);
			// open DocuSign OAuth authorization url in the browser, login and grant access
			//Desktop.getDesktop().browse(URI.create(oauthLoginUrl));
			// END OF NOTE

			java.util.List<String> scopes = new ArrayList<String>();
			scopes.add(OAuth.Scope_SIGNATURE);

			OAuth.OAuthToken oAuthToken = apiClient.requestJWTUserToken(integrationkey, username, scopes, fileContent, 3600);
			// now that the API client has an OAuth token, let's use it in all
			// DocuSign APIs
			apiClient.setAccessToken(oAuthToken.getAccessToken(), oAuthToken.getExpiresIn());
			UserInfo userInfo = apiClient.getUserInfo(oAuthToken.getAccessToken());

			// parse first account's baseUrl
			// below code required for production, no effect in demo (same
			// domain)
			apiClient.setBasePath(userInfo.getAccounts().get(0).getBaseUri() + "/restapi");
			Configuration.setDefaultApiClient(apiClient);
			String accountId = userInfo.getAccounts().get(0).getAccountId();

            RecipientViewRequest recipientView = new RecipientViewRequest();
			recipientView.setReturnUrl(returnurl);
			recipientView.setClientUserId(receipient.getEmailId());
			recipientView.setAuthenticationMethod("email");
			recipientView.setUserName(receipient.getName());
			recipientView.setEmail(receipient.getEmailId());

			EnvelopesApi envelopesApi = new EnvelopesApi();

			ViewUrl viewUrl = envelopesApi.createRecipientView(accountId, envelopeId, recipientView);
            map.put("viewUrl", viewUrl.getUrl());
        }catch(Exception e){
            map = null;
            e.printStackTrace();
        }
        return map;
    }

    public List<Receipient> getJson(String receipient) throws JsonMappingException, JsonProcessingException{
        List<Receipient> list = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        list = Arrays.asList(objectMapper.readValue(receipient, Receipient[].class));
        return list;
    }
}
