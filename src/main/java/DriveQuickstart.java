// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// [START drive_quickstart]
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/* class to demonstarte use of Drive files list API */
public class DriveQuickstart {
    /** Application name. */
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /** Directory to store authorization tokens for this application. */
    private static final String TOKENS_DIRECTORY_PATH = "resources";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     *
     * En el ejemplo original esta readonly metadatos, por lo tanto si lo dejamos asi
     * no podremos descargar ficheros, solo listarlos
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("330911191559-hn51kmg7gr79r110c0e6oj525qchvbqv.apps.googleusercontent.com");
        //returns an authorized Credential object.
        return credential;
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Filtra para encontrar la carpeta que se llama Imagen_BOT
        FileList result = service.files().list()
                .setQ("name contains 'ExamenBOT' and mimeType = 'application/vnd.google-apps.folder'")
                .setPageSize(100)
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();

        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            String dirImagenes = null;
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
                dirImagenes = file.getId();
            }
            // busco la imagen en el directorio
            FileList resultImagenes = service.files().list()
                    .setQ("name contains 'Examen' and parents in '"+dirImagenes+"'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> filesImagenes = resultImagenes.getFiles();

            for (File file : filesImagenes) {
                System.out.printf("Imagen: %s\n", file.getName());
                // guardamos el 'stream' en el fichero aux.jpeg
                OutputStream outputStream = new FileOutputStream("/Users/laura/proyectosCOD/Api/src/main/java/PDF/prueba.pdf");
                service.files().export(file.getId(), "application/pdf")
                        .executeMediaAndDownloadTo(outputStream);

                outputStream.flush();
                outputStream.close();
            }
        }


        final String token = "OTgyMDA3NjI5NTc4NjY1OTg1.GhcJNZ.x9JExQZpj7bocsMq6c3arZfzBMkBz1W6UA4N8w";
        final DiscordClient client = DiscordClient.create(token);
        final GatewayDiscordClient gateway = client.login().block();

           /*
          Creamos 3 constantes:
          - Variable String para almacenar el token.
          - Se crea el cliente utilazando el token
          - Se crea un getaway(pasarela) usando el cliente.


      Creamos el mensaje embed donde nos da el titulo y la imagen del mismo.

            */


        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .title("Catinho")
                .image("attachment://loki.jpg")
                .build();

        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            final Message message = event.getMessage();

        /*
          Con el bot ejecutándose podremos enviar un mensaje: !ping. Y el nos responderá: !pong.
         */

            if ("!ping".equals(message.getContent())) {
                final MessageChannel channel = message.getChannel().block();
                channel.createMessage("Pong!").block();
            }



       /* Aqui está el funcionamiento del !embed que es muy parecido al !ping y Pong! solo que nos devuelve un embed
        en vez de un mensaje.

        */


            if ("!embed".equals(message.getContent())) {
                String IMAGE_URL = "https://i.imgflip.com/1d4rel.jpg";
                String ANY_URL = "https://www.youtube.com/watch?v=0HVI9Zr3FgY";
                final MessageChannel channel = message.getChannel().block();
                EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();
                builder.author("¿Pues quién va a ser? Laura", ANY_URL, IMAGE_URL);
                builder.image(IMAGE_URL);
                builder.title("Mi querido Bot");
                builder.url(ANY_URL);
                builder.description("Bienvenidos al canal, sentaos y poneos cómodos. Empieza el camino del terror");
                builder.thumbnail(IMAGE_URL);
                builder.footer("BOO", IMAGE_URL);
                builder.timestamp(Instant.now());
                channel.createMessage(builder.build()).block();

            }


          /*Si escribimos "!pdf" el bot mandará un pdf y está en un try catch por si no encuentra el pdf
          (FileNotFoundException).

           */


            if ("!pdf".equals(message.getContent())) {
                final MessageChannel channel = message.getChannel().block();

                InputStream fileAsInputStream = null;
                try {
                    fileAsInputStream = new FileInputStream("/Users/laura/proyectosCOD/Api/src/main/java/PDF/prueba.pdf");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                channel.createMessage(MessageCreateSpec.builder()
                        .content("Lindo pedefiño")
                        .addFile("/Users/laura/proyectosCOD/Api/src/main/java/PDF/prueba.pdf", fileAsInputStream)
                        .addEmbed(embed)
                        .build()).subscribe();
            }
        });

        gateway.onDisconnect().block();



    }
}
// [END drive_quickstart]