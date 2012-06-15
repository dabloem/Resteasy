package org.jboss.resteasy.test.nextgen.finegrain.methodparams;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.test.BaseResourceTest;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Set;

/**
 * Spec requires that HEAD and OPTIONS are handled in a default manner
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PathParamTest extends BaseResourceTest
{

   @Path(value = "/PathParamTest")
   public static class Resource
   {
      @GET
      @Path("/{id}")
      public Response single(@PathParam("id") String id)
      {
         return Response.ok("single=" + id).build();
      }

      @GET
      @Path("/{id}/{id1}")
      public Response two(@PathParam("id") String id,
                          @PathParam("id1") PathSegment id1)
      {
         return Response.ok("double=" + id + id1.getPath()).build();
      }

      @GET
      @Path("/{id}/{id1}/{id2}")
      public Response triple(@PathParam("id") int id,
                             @PathParam("id1") PathSegment id1,
                             @PathParam("id2") float id2)
      {
         return Response.ok("triple=" + id + id1.getPath() + id2).build();
      }

      @GET
      @Path("/{id}/{id1}/{id2}/{id3}")
      public Response quard(@PathParam("id") double id,
                            @PathParam("id1") boolean id1,
                            @PathParam("id2") byte id2,
                            @PathParam("id3") PathSegment id3)
      {
         return Response.ok("quard=" + id + id1 + id2 + id3.getPath()).build();
      }

      @GET
      @Path("/{id}/{id1}/{id2}/{id3}/{id4}")
      public Response penta(@PathParam("id") long id,
                            @PathParam("id1") String id1,
                            @PathParam("id2") short id2,
                            @PathParam("id3") boolean id3,
                            @PathParam("id4") PathSegment id4)
      {
         return Response.ok("penta=" + id + id1 + id2 + id3 + id4.getPath()).
                 build();
      }

      @Produces("text/plain")
      @GET
      @Path("/{id}/{id}/{id}/{id}/{id}/{id}")
      public Response list(@PathParam("id") List<String> id)
      {
         StringBuffer sb = new StringBuffer();
         sb.append("list=");
         for (String tmp : id)
         {
            sb.append(tmp);
         }
         return Response.ok(sb.toString()).build();
      }

      @Produces("text/plain")
      @GET
      @Path("/matrix/{id}")
      public Response matrixparamtest(@PathParam("id") PathSegment id)
      {
         StringBuffer sb = new StringBuffer();
         sb.append("matrix=");

         sb.append("/" + id.getPath());
         MultivaluedMap<String, String> matrix = id.getMatrixParameters();
         Set keys = matrix.keySet();
         for (Object key : keys)
         {
            sb.append(";" + key.toString() + "=" +
                    matrix.getFirst(key.toString()));

         }
         return Response.ok(sb.toString()).build();
      }
   }

   @Path("/digits")
   public static class Digits
   {
      @Path("{id:\\d+}")
      @GET
      public String get(@PathParam("id") int id)
      {
         Assert.assertEquals(5150, id);
         return Integer.toString(id);
      }
   }

   @Before
   public void setUp() throws Exception
   {
      deployment.getRegistry().addPerRequestResource(Digits.class);
      deployment.getRegistry().addPerRequestResource(Resource.class);
      deployment.getRegistry().addPerRequestResource(CarResource.class);
   }

   /*
    * Client invokes GET on root resource at /PathParamTest;
    *                 Verify that right Method is invoked using
    *                 PathParam primitive type List<String>.
    */
   @Test
   public void test6() throws Exception
   {

      String[] Headers = {"list=abcdef"};//, "list=fedcba"};

      ResteasyClient client = new ResteasyClient();
      for (String header : Headers)
      {
         Invocation.Builder request = client.target(TestPortProvider.generateURL("/PathParamTest/a/b/c/d/e/f")).request();
         request.header("Accept", "text/plain");
         Response response = request.get();
         Assert.assertEquals(200, response.getStatus());
         Assert.assertEquals(header, response.readEntity(String.class));
      }
   }

   @Test
   public void test178() throws Exception
   {
      ResteasyClient client = new ResteasyClient();
      {
         Invocation.Builder request = client.target(TestPortProvider.generateURL("/digits/5150")).request();
         Response response = request.get();
         Assert.assertEquals(200, response.getStatus());
         response.close();
      }
      
      {
         Invocation.Builder request = client.target(TestPortProvider.generateURL("/digits/5150A")).request();
         Response response = request.get();
         Assert.assertEquals(404, response.getStatus());
         response.close();
      }
   }

   @Path("/cars/{make}")
   public static class CarResource
   {
      public static enum Color
      {
         red,
         white,
         blue,
         black
      }

      @GET
      @Path("/matrixparam/{model}/{year}")
      @Produces("text/plain")
      public String getFromMatrixParam(@PathParam("make") String make,
                                       @PathParam("model") PathSegment car,
                                       @MatrixParam("color") Color color,
                                       @PathParam("year") String year)
      {
         return "A " + color + " " + year + " " + make + " " + car.getPath();
      }


      @GET
      @Path("/pathsegment/{model}/{year}")
      @Produces("text/plain")
      public String getFromPathSegment(@PathParam("make") String make,
                                       @PathParam("model") PathSegment car,
                                       @PathParam("year") String year)
      {
         String carColor = car.getMatrixParameters().getFirst("color");
         return "A " + carColor + " " + year + " " + make + " " + car.getPath();
      }

      @GET
      @Path("/pathsegments/{model : .+}/year/{year}")
      @Produces("text/plain")
      public String getFromMultipleSegments(@PathParam("make") String make,
                                            @PathParam("model") List<PathSegment> car,
                                            @PathParam("year") String year)
      {
         String output = "A " + year + " " + make;
         for (PathSegment segment : car)
         {
            output += " " + segment.getPath();
         }
         return output;
      }

      @GET
      @Path("/uriinfo/{model}/{year}")
      @Produces("text/plain")
      public String getFromUriInfo(@Context UriInfo info)
      {
         String make = info.getPathParameters().getFirst("make");
         String year = info.getPathParameters().getFirst("year");
         PathSegment model = info.getPathSegments().get(3);
         String color = model.getMatrixParameters().getFirst("color");

         return "A " + color + " " + year + " " + make + " " + model.getPath();
      }
   }

   @Test
   public void testCarResource() throws Exception
   {
      ResteasyClient client = new ResteasyClient();
      System.out.println("**** Via @MatrixParam ***");
      Invocation.Builder request = client.target(TestPortProvider.generateURL("/cars/mercedes/matrixparam/e55;color=black/2006")).request();
      Response response = request.get();
      Assert.assertEquals(200, response.getStatus());
      Assert.assertEquals("A black 2006 mercedes e55", response.readEntity(String.class));
      // This must be a typo.  Should be "A midnight blue 2006 Porsche 911 Carrera S".

      System.out.println("**** Via PathSegment ***");
      request = client.target(TestPortProvider.generateURL("/cars/mercedes/pathsegment/e55;color=black/2006")).request();
      response = request.get();
      Assert.assertEquals(200, response.getStatus());
      Assert.assertEquals("A black 2006 mercedes e55", response.readEntity(String.class));

      System.out.println("**** Via PathSegments ***");
      request = client.target(TestPortProvider.generateURL("/cars/mercedes/pathsegments/e55/amg/year/2006")).request();
      response = request.get();
      Assert.assertEquals(200, response.getStatus());
      Assert.assertEquals("A 2006 mercedes e55 amg", response.readEntity(String.class));

      System.out.println("**** Via PathSegment ***");
      request = client.target(TestPortProvider.generateURL("/cars/mercedes/uriinfo/e55;color=black/2006")).request();
      response = request.get();
      Assert.assertEquals(200, response.getStatus());
      Assert.assertEquals("A black 2006 mercedes e55", response.readEntity(String.class));
   }


}