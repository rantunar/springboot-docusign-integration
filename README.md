# springboot-docusign-integration

<b>It's a sample spring boot application to do the integration btween docusign and java SDK. There are 2 steps has been involved for the integration- 1. call the embadedSending API with the attached documents and the receipient list those are going to sing the doc, in response envolope id and view url will be generated, view url can be opened in browser to place the signature location for all the receipient. 2. Call the embeddedSignning api with same envolope id generated from above api and pass the receipient details who is going to sign those docs, in response view url will be provided that will be used to do digital signning to the exact place which is applicable for that receipient only.

All the API interaction with docusign account need to authorized so for that docusign account details must be provided in application.properties file as described in below steps.</b>


## Steps:
1. git clone
2. Login to docusign portal using user details
3. Go to profile > Apps and Keys > Choose an app
4. Click on Action > Edit > copy integration key > Generate RSA key and copy private key
5. Go to user in left hand mend and copy username
6. Paste the rsa private key in project- resoures/static/privatekey.txt
7. Open application.properties and update docusign.server.integrationkey with the integration key copied from docusign portal.
8. Open application.properties and update docusign.server.username with the username copied from docusign portal.
9. Run the application

## Request
### Embadded Sending
`
{"client":"Thunder Client","collectionName":"embeddedSending","dateExported":"2021-10-09T10:01:38.424Z","version":"1.1","folders":[],"requests":[{"containerId":"","sortNum":10000,"headers":[{"name":"Accept","value":"*/*"},{"name":"User-Agent","value":"Thunder Client (https://www.thunderclient.io)"}],"colId":"21f8b534-6ba8-475d-8a3f-c744b33b4360","name":"embeddedSending","url":"http://localhost:8000/docusign/embeddedSending","method":"POST","modified":"2021-10-09T10:01:24.223Z","created":"2021-10-08T07:09:04.464Z","_id":"026e133c-4e16-45b4-bea0-cb67fbd0468e","params":[],"body":{"type":"formdata","raw":"","form":[{"name":"receipient","value":"[{\"name\":\"mkyong\", \"emailId\":\"mykong@doc.com\"}, {\"name\":\"fong\", \"emailId\":\"fong@doc.com\"}]"}],"files":[{"name":"file","value":"C:\\Users\\RIYA\\Documents\\docusign\\demo\\src\\test\\docs\\SignTest1.pdf"},{"name":"file","value":"C:\\Users\\RIYA\\Documents\\docusign\\demo\\src\\test\\docs\\sign.pdf"}]},"tests":[]}]}`

## Response
`
{
  "documents": [
    {
      "docName": "file",
      "docOriginalName": "SignTest1.pdf",
      "docId": "1"
    },
    {
      "docName": "file",
      "docOriginalName": "sign.pdf",
      "docId": "2"
    }
  ],
  "envelopeId": "e19c53f9-5aeb-4dc3-8c75-a5f9b3d7645e",
  "viewUrl": "https://appdemo.docusign.com/auth-from-console?code=87fcb156-7a48-424b-a831-bbdf90a88172&t=51fa7581-4178-4684-9e6e-2119153b7cbb&from=https%3A%2F%2Fdemo.docusign.net&view=true&DocuEnvelope=e19c53f9-5aeb-4dc3-8c75-a5f9b3d7645e&e=e19c53f9-5aeb-4dc3-8c75-a5f9b3d7645e&send=1&accountId=72f54f8c-d2b1-4de8-b09c-98ab3dec5efa&a=tag"
}`

## Request
### Embedded Signning
`{"client":"Thunder Client","collectionName":"embeddedSignning","dateExported":"2021-10-09T10:14:31.662Z","version":"1.1","folders":[],"requests":[{"containerId":"","sortNum":10000,"headers":[{"name":"Accept","value":"*/*"},{"name":"User-Agent","value":"Thunder Client (https://www.thunderclient.io)"}],"colId":"e58b8477-7f41-4141-b66b-84e3d4568389","name":"embeddedSignning","url":"http://localhost:8000/docusign/embeddedSignning?envelopeId=9e096234-2efe-4645-b02e-f3df87da6872","method":"POST","modified":"2021-10-09T10:14:25.638Z","created":"2021-10-08T08:54:54.465Z","_id":"0816d4f3-01ab-412f-9b7a-f8ee081f0fa5","params":[{"name":"envelopeId","value":"9e096234-2efe-4645-b02e-f3df87da6872","isPath":false}],"body":{"type":"json","raw":"{\n  \"name\": \"mkyong\",\n  \"emailId\": \"mykong@doc.com\"\n}","form":[]},"tests":[]}]}
`