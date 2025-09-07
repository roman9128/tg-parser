package rt.infrastructure.analyzer.nlp;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

class NLPModelFinder {

    private final Map<String, DocumentCategorizerME> models = new HashMap<>();

    void findModels() {

        String modelsDirString = "nlp/models";
        String modelExtension = "model";
        Path modelsDir = Paths.get(modelsDirString);

        if (!Files.exists(modelsDir) || !Files.isDirectory(modelsDir)) {
            throw new RuntimeException("Указанный путь не является директорией или не существует");
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(modelsDir, "*.{" + modelExtension + "}")) {
            for (Path path : stream) {
                File modelFile = path.toFile();
                String fileName = modelFile.getName();
                String label = fileName.substring(0, fileName.length() - modelExtension.length() - 1); // удаляю расширение .model
                DoccatModel model = new DoccatModel(modelFile);
                models.put(label, new DocumentCategorizerME(model));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    Map<String, DocumentCategorizerME> getModels() {
        return models;
    }
}
