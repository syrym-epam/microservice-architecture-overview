package com.microservice.resource_processor;


@Service
public class ServiceResourceProcessor {

    Metadata metadata = extractMetadataFromMP3File(data);


    public Metadata makeMetadataFromClass(Object obj) {
        Metadata metadata = new Metadata();

        Field[] attributes = obj.getClass().getDeclaredFields();
        for(int i = 1; i< attributes.length; i++) {
            try {
                metadata.set(attributes[i].getName(), "" + Optional.ofNullable(attributes[i].get(obj)).orElse(""));
            } catch (IllegalArgumentException e) {
                continue;
            } catch (IllegalAccessException e) {
                continue;
            }
        }

        return metadata;
    }

    public Optional<String> calcDurationMP3(String value) {
        if(value == null)
            return Optional.empty();
        else {
            double second = Double.parseDouble(value);
            int totalSecs = (int) Math.round(second);
            int mins = totalSecs / 60;
            int secs = totalSecs % 60;
            return Optional.of("%02d:%02d".formatted(mins, secs));
        }
    }

    public Metadata extractMetadataFromMP3File(byte[] data){
        Metadata metaData = new Metadata();
        try {
            BodyContentHandler handler = new BodyContentHandler();
            ParseContext context = new ParseContext();
            Mp3Parser parser = new Mp3Parser();
            parser.parse(new ByteArrayInputStream(data), handler, metaData, context);
        } catch(Exception e) {
            return metaData;
        }

        return metaData;
    }


}