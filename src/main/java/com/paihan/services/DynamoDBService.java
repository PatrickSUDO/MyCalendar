package com.paihan.services;

import com.paihan.entities.WorkItem;
import com.sun.tools.javac.util.Name;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import org.springframework.stereotype.Component;



/*
 Before running this code example, create a table named Work with a PK named id
 */
@Component
public class DynamoDBService {

    private DynamoDbClient getClient() {
        // Create a DynamoDbClient object
        Region region = Region.EU_WEST_1;
        DynamoDbClient ddb = DynamoDbClient.builder()
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .region(region)
                .build();

        return ddb;
    }

    // Get a single item from the Work table based on the Key
    public String getItem(String idValue) {

        DynamoDbClient ddb = getClient();
        String event = "";
        String eventDate = "";
        String description = "";

        HashMap<String, AttributeValue> keyToGet = new HashMap<String,AttributeValue>();
        keyToGet.put("id", AttributeValue.builder()
                .s(idValue)
                .build());

        // Create a GetItemRequest object
        GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName("Work")
                .build();

        try {
            Map<String,AttributeValue> returnedItem = ddb.getItem(request).item();

            // Get keys and values and get description and status
            for (Map.Entry<String,AttributeValue > entry : returnedItem.entrySet()) {
                String k = entry.getKey();
                AttributeValue v = entry.getValue();

                //if the attribute names are equal
                if (k.compareTo("event") ==  0) {
                    event = v.s();
                } else if (k.compareTo("eventDate") == 0) {
                    eventDate = v.s();
                } else if (k.compareTo("description") == 0) {
                    description = v.s();
                }
            }
            return convertToString(toXmlItem(idValue,event,eventDate,description));

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return "";
    }

    // Retrieves items from the DynamoDB table
    public  ArrayList<WorkItem> getListItems() {

        // Create a DynamoDbEnhancedClient
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(getClient())
                .build();

        try {
            // Create a DynamoDbTable object
            DynamoDbTable<Work> custTable = enhancedClient.table("Work", TableSchema.fromBean(Work.class));

            // Get items in the Work table
            Iterator<Work> results = custTable.scan().items().iterator();
            WorkItem workItem ;
            ArrayList<WorkItem> itemList = new ArrayList();

            while (results.hasNext()) {

                // Populate a WorkItem
                workItem = new WorkItem();
                Work work = results.next();
                workItem.setId(work.getId());
                workItem.setName(work.getName());
                workItem.setEvent(work.getEvent());
                workItem.setEventDate(work.getEventDate());
                workItem.setDescription(work.getDescription());
                workItem.setDate(work.getDate());

                //Push the workItem to the list

                itemList.add(workItem);
            }

            return itemList;
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Done");

        return null ;
    }


    // Updates items status based on id in the Work table
    public String UpdateItem(String id, String event, String eventDate, String description){
        DynamoDbClient ddb = getClient();

        HashMap<String,AttributeValue> itemKey = new HashMap<String,AttributeValue>();

        itemKey.put("id", AttributeValue.builder()
                .s(id)
                .build());

        HashMap<String, AttributeValueUpdate> updatedValues =
                new HashMap<String,AttributeValueUpdate>();

        // Update the column specified by name with updatedVal
        updatedValues.put("event", AttributeValueUpdate.builder()
                .value(AttributeValue.builder()
                        .s(event).build())
                .action(AttributeAction.PUT)
                .build());

        updatedValues.put("eventDate", AttributeValueUpdate.builder()
                .value(AttributeValue.builder()
                        .s(eventDate).build())
                .action(AttributeAction.PUT)
                .build());

        updatedValues.put("description", AttributeValueUpdate.builder()
                .value(AttributeValue.builder()
                        .s(description).build())
                .action(AttributeAction.PUT)
                .build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName("Work")
                .key(itemKey)
                .attributeUpdates(updatedValues)
                .build();

        try {
            ddb.updateItem(request);
            return"The Status for the item was successfully updated";
        } catch (ResourceNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return "";
    }

    public String deleteItemWithId(String id){

        DynamoDbClient ddb = getClient();

        HashMap<String, AttributeValue> itemKey = new HashMap<String,AttributeValue>();
        itemKey.put("id", AttributeValue.builder()
                .s(id)
                .build());

        // Create a DeleteItemRequest object
        DeleteItemRequest request = DeleteItemRequest.builder()
                .key(itemKey)
                .tableName("Work")
                .build();

        try {
            ddb.deleteItem(request);
            return "The Status for the item was successfully deleted";

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return "";

    }

    // Get Open items from the DynamoDB table
    public String getAllItems() {

        // Create a DynamoDbEnhancedClient

        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(getClient())
                .build();

        try{
            // Create a DynamoDbTable object
            DynamoDbTable<Work> table = enhancedClient.table("Work", TableSchema.fromBean(Work.class));

            // Get items in the Work table
            Iterator<Work> results = table.scan().items().iterator();
            WorkItem workItem ;
            ArrayList<WorkItem> itemList = new ArrayList();
            while (results.hasNext()) {

                // Populate a WorkItem
                workItem = new WorkItem();
                Work work = results.next();

                workItem.setId(work.getId());
                workItem.setName(work.getName());
                workItem.setEvent(work.getEvent());
                workItem.setEventDate(work.getEventDate());
                workItem.setDescription(work.getDescription());
                workItem.setDate(work.getDate());


                // Push the workItem to the list
                itemList.add(workItem);
            }
            return convertToString(toXml(itemList));

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Done");

        return "" ;
    }


    public void setItem(WorkItem item) {

        // Create a DynamoDbEnhancedClient
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(getClient())
                .build();

        putRecord(enhancedClient, item) ;
    }

    // Put an item into a DynamoDB table
    public void putRecord(DynamoDbEnhancedClient enhancedClient, WorkItem item) {

        try {

            // Create a DynamoDbTable object
            DynamoDbTable<Work> workTable = enhancedClient.table("Work", TableSchema.fromBean(Work.class));

            // Create an Instant object
            LocalDate localDate = LocalDate.parse("2020-04-07");
            LocalDateTime localDateTime = localDate.atStartOfDay();
            Instant instant = localDateTime.toInstant(ZoneOffset.UTC);

            String myGuid = java.util.UUID.randomUUID().toString();

            // Populate the table
            Work record = new Work();

            record.setId(myGuid);
            record.setUsername(item.getName());
            record.setEvent(item.getEvent());
            record.setEventDate(item.getEventDate());
            record.setDescription(item.getDescription());
            record.setDate(now()) ;
            // Put the customer data into a DynamoDB table
            workTable.putItem(record);

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    // Convert Work data into XML to pass back to the view
    private Document toXml(List<WorkItem> itemList) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Start building the XML
            Element root = doc.createElement( "Items" );
            doc.appendChild( root );

            // Get the elements from the collection
            int custCount = itemList.size();

            // Iterate through the collection
            for ( int index=0; index < custCount; index++) {

                // Get the WorkItem object from the collection
                WorkItem myItem = itemList.get(index);

                Element item = doc.createElement( "Item" );
                root.appendChild( item );

                // Set Id
                Element id = doc.createElement( "Id" );
                id.appendChild( doc.createTextNode(myItem.getId() ) );
                item.appendChild( id );

                // Set Name
                Element name = doc.createElement( "Name" );
                name.appendChild( doc.createTextNode(myItem.getName() ) );
                item.appendChild( name );

                // Set event
                Element event = doc.createElement( "Event" );
                event.appendChild( doc.createTextNode(myItem.getEvent() ) );
                item.appendChild( event );

                // Set event date
                Element eventDate = doc.createElement( "EventDate" );
                eventDate.appendChild( doc.createTextNode(myItem.getEventDate()));
                item.appendChild( eventDate );

                // Set Description
                Element desc = doc.createElement( "Description" );
                desc.appendChild( doc.createTextNode(myItem.getDescription() ) );
                item.appendChild( desc );

                // Set Date
                Element date = doc.createElement( "Date" );
                date.appendChild( doc.createTextNode(myItem.getDate() ) );
                item.appendChild( date );


            }
            return doc;
        } catch(ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String convertToString(Document xml) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(xml);
            transformer.transform(source, result);
            return result.getWriter().toString();

        } catch(TransformerException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private String now() {
        String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }

    // Convert Work data into an XML schema to pass back to client
    private Document toXmlItem(String id2, String event2,
                               String eventDate2, String description2) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Start building the XML
            Element root = doc.createElement( "Items" );
            doc.appendChild( root );

            Element item = doc.createElement( "Item" );
            root.appendChild( item );

            // Set Id
            Element id = doc.createElement( "Id" );
            id.appendChild( doc.createTextNode(id2 ) );
            item.appendChild( id );

            // Set Event
            Element event = doc.createElement( "Event" );
            event.appendChild( doc.createTextNode(event2) );
            item.appendChild( event );

            // Set Event Date
            Element eventDate = doc.createElement( "eventDate" );
            eventDate.appendChild( doc.createTextNode(eventDate2) );
            item.appendChild(eventDate);

            // Set Description
            Element description = doc.createElement( "Description" );
            description.appendChild( doc.createTextNode(description2) );
            item.appendChild( description );

            return doc;

        } catch(ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
