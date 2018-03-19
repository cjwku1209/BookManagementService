package com.calvinku;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;



@Path("/BookManagementService/books")
public class LibraryManagementSystem {

    @GET
    @Path("{id}")
    public Response idLookup(@PathParam("id") String id){
        MongoDBEngine database = new MongoDBEngine();
        ArrayList<Document> data = null;
        try{
            data = MongoDBEngine.get(database.getCollection("books").find(new BasicDBObject().append("_id", new ObjectId(id))).projection(new BasicDBObject().append("_id", 0)));
        }catch (Exception e){
            return Response.status(204).entity("").build();
        }
        if(data == null || data.size() == 0){
            return Response.status(204).entity("").build();
        }
        else{
            JSONArray output = new JSONArray(data);
            JSONObject outputJSON = new JSONObject();
            outputJSON.put("FoundBooks", data.size());
            outputJSON.put("Results", output);
            return Response.status(201).entity(outputJSON.toString()).build();
        }
    }

    @GET
    public Response lookup(@Context HttpServletRequest request) {
        MongoDBEngine database = new MongoDBEngine();
        ArrayList<Document> data = null;
        String query = request.getQueryString();
        Gson gson = new Gson();
        JSONObject jsonObject = new JSONObject(gson.toJson(request.getParameterMap()));
        BasicDBObject criteria = new BasicDBObject();
        int limit = 0;
        if (query.equals("")) {
            data = MongoDBEngine.get(database.getCollection("books").find().projection(new BasicDBObject().append("_id", 0).append("Available", 0)));
            JSONArray output = new JSONArray(data);
            JSONObject outputJSON = new JSONObject();
            outputJSON.put("FoundBooks", data.size());
            outputJSON.put("Results", output);
            return Response.status(201).entity(outputJSON.toString()).build();
        }
        if (jsonObject.has("id")) {
            criteria.append("_id", new ObjectId((String) jsonObject.getJSONArray("id").get(0).toString()));
        }
        if (jsonObject.has("author")) {
            String regex = jsonObject.getJSONArray("author").get(0).toString();
            criteria.append("Author", Pattern.compile(regex));
        }
        if (jsonObject.has("publisher")) {
            String regex = jsonObject.getJSONArray("publisher").get(0).toString();
            criteria.append("Publisher", Pattern.compile(regex));
        }
        if (jsonObject.has("year")) {
            String regex = jsonObject.getJSONArray("year").get(0).toString();
            criteria.append("Year", Pattern.compile(regex));
        }
        if (jsonObject.has("title")) {
            String regex = jsonObject.getJSONArray("title").get(0).toString();
            criteria.append("Title", Pattern.compile(regex));
        }
        if (jsonObject.has("limit")) {
            limit = Integer.parseInt(jsonObject.getJSONArray("limit").get(0).toString());
        }

        try{
            if (jsonObject.has("sortby")) {
                String sortby = jsonObject.getJSONArray("sortby").get(0).toString();
                int order = 1;
                if (jsonObject.has("order")){
                    String orderString =  jsonObject.getJSONArray("order").get(0).toString();
                    if (orderString.equals("asc")) {
                        order = 1;
                    }
                    else if (orderString.equals("desc")) {
                        order = -1;
                    }
                    else {
                        return Response.status(204).entity("").build();
                    }
                }
                if(sortby.equals("id")){ ;
                    data = MongoDBEngine.get(database.getCollection("books").find(criteria).sort(new BasicDBObject("_id", order)).projection(new BasicDBObject().append("_id", 0)).limit(limit).projection(new BasicDBObject().append("_id", 0).append("Available", 0)));
                }
                else if(sortby.equals("author")){
                    data = MongoDBEngine.get(database.getCollection("books").find(criteria).sort(new BasicDBObject("Author", order)).projection(new BasicDBObject().append("_id", 0)).limit(limit).projection(new BasicDBObject().append("_id", 0).append("Available", 0)));
                }
                else if(sortby.equals("title")){
                    data = MongoDBEngine.get(database.getCollection("books").find(criteria).sort(new BasicDBObject("Title", order)).projection(new BasicDBObject().append("_id", 0)).limit(limit).projection(new BasicDBObject().append("_id", 0).append("Available", 0)));
                }
                else if(sortby.equals("publisher")){
                    data = MongoDBEngine.get(database.getCollection("books").find(criteria).sort(new BasicDBObject("Publisher", order)).projection(new BasicDBObject().append("_id", 0)).limit(limit).projection(new BasicDBObject().append("_id", 0).append("Available", 0)));
                }
                else if(sortby.equals("year")){
                    data = MongoDBEngine.get(database.getCollection("books").find(criteria).sort(new BasicDBObject("Year", order)).projection(new BasicDBObject().append("_id", 0)).limit(limit).projection(new BasicDBObject().append("_id", 0).append("Available", 0)));
                }
                else {
                    return Response.status(204).entity("").build();
                }
            }
            else {
                data = MongoDBEngine.get(database.getCollection("books").find(criteria).projection(new BasicDBObject().append("_id", 0)).limit(limit).projection(new BasicDBObject().append("_id", 0).append("Available", 0)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(204).entity("").build();
        }
        JSONArray output = new JSONArray(data);
        JSONObject outputJSON = new JSONObject();
        outputJSON.put("FoundBooks", data.size());
        outputJSON.put("Results", output);
        return Response.status(201).entity(outputJSON.toString()).build();
    }

    @POST
    @Consumes("application/json")
    public Response AddBooks(InputStream incomingData) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (Exception e) {
            System.out.println("Error Parsing: - ");
        }

        MongoDBEngine database = new MongoDBEngine();
        System.out.println(stringBuilder.toString());
        Document document = Document.parse(stringBuilder.toString());

        if( MongoDBEngine.get(database.getCollection("books").find(document)).size() != 0 ){
            String output = "Duplicated record: /books/" + MongoDBEngine.get(database.getCollection("books").find(document)).get(0).get("_id");
            return Response.status(409).entity(output).build();
        }
        else{
            document.put("Available", true);
            database.getCollection("books").insertOne(document);
            String output = "Location: /books/" + document.get("_id");
            System.out.print(output);
            return Response.status(HttpStatus.CREATED_201).entity(output).build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes("application/json")
    public Response loaningAndReturning(@PathParam("id") String id, InputStream incomingData){
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (Exception e) {
            System.out.println("Error");
        }
        JSONObject jsonObject = new JSONObject(stringBuilder.toString());
        System.out.println(jsonObject);
        String avail = jsonObject.get("Available").toString();
        boolean availBool = Boolean.parseBoolean(avail);
        MongoDBEngine database = new MongoDBEngine();
        ArrayList<Document> book = null;
        try{
            book = MongoDBEngine.get(database.getCollection("books").find(new BasicDBObject().append("_id", new ObjectId(id))).projection(new BasicDBObject().append("_id", 0)));
        }catch (Exception e){
            return Response.status(404).entity("No book record").build();
        }
        if(book == null || book.size() == 0){
            return Response.status(HttpStatus.NOT_FOUND_404).entity("No book record").build();
        }
        else{
            try {
                if(availBool == book.get(0).getBoolean("Available")) {
                    database.getCollection("books").updateOne(new BasicDBObject().append("_id", new ObjectId(id)), new BasicDBObject().append("$set", new BasicDBObject().append("Available", !availBool)));
                    return Response.status(HttpStatus.OK_200).entity("").build();

                }
                else{
                    return Response.status(HttpStatus.NOT_FOUND_404).entity("").build();
                }
            }catch(Exception e){
                e.printStackTrace();
                return Response.status(HttpStatus.NOT_FOUND_404).entity("").build();
            }
        }
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") String id){
        MongoDBEngine database = new MongoDBEngine();
        ArrayList<Document> book = null;
        try{
            book = MongoDBEngine.get(database.getCollection("books").find(new BasicDBObject().append("_id", new ObjectId(id))).projection(new BasicDBObject().append("_id", 0)));
        }catch (Exception e){
            return Response.status(HttpStatus.NOT_FOUND_404).entity("No book record").build();
        }
        if(book == null || book.size() == 0){
            return Response.status(HttpStatus.NOT_FOUND_404).entity("No book record").build();
        }
        else{
            database.getCollection("books").deleteOne(new BasicDBObject().append("_id", new ObjectId(id)));
            return Response.status(HttpStatus.OK_200).entity("").build();
        }
    }
}
