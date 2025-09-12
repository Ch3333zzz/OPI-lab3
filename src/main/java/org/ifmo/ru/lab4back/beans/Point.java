package org.ifmo.ru.lab4back.beans;

import com.google.gson.Gson;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.ifmo.ru.lab4back.EJB.PointEJB;
import org.ifmo.ru.lab4back.entities.PointEntity;
import org.ifmo.ru.lab4back.util.Messages;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
@Path("/point")
public class Point {
    public Point() {
    }

    private String x;
    private String y;
    private String r;

    @EJB
    private PointEJB pointEJB;

    @POST
    @Path("/add-point")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addPoint(PointEntity point) {
        try {
            long startTime = System.nanoTime();

            String hit = checkHit(point.getX(), point.getY(), point.getR());

            point.setHit(hit);

            LocalDateTime currentTime = LocalDateTime.now();

            long executionTime = (System.nanoTime() - startTime) / 1000;

            point.setExecutionTime(executionTime);
            point.setCurrentTime(currentTime.toString());

            pointEJB.createPoint(point);
            System.out.println(point.toString());

            PointEntity responsePoint = new PointEntity();
            responsePoint.setX(point.getX());
            responsePoint.setY(point.getY());
            responsePoint.setR(point.getR());
            responsePoint.setHit(hit);
            responsePoint.setExecutionTime(executionTime);
            responsePoint.setCurrentTime(currentTime.toString());

            pointEJB.getAllPoints().forEach(System.out::println);

            return Response.status(Response.Status.OK)
                    .entity(responsePoint)
                    .build();

        } catch (Exception e) {
            String msg = Messages.get("error.processing.point");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @GET
    @Path("/get-points/{login}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserPoints(@PathParam("login") String userLogin) {
        if (userLogin == null || userLogin.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        pointEJB.getAllPoints().forEach(System.out::println);
        List<PointEntity> userPoints = pointEJB.getPointsByOwner(userLogin);
        Gson gson = new Gson();
        String json = gson.toJson(userPoints);
        System.out.println(json);
        return Response.status(Response.Status.OK)
                .entity(userPoints)
                .build();
    }

    public String checkHit(String xVal, String yVal, String rVal) throws NumberFormatException {
        double x = Double.parseDouble(xVal);
        double y = Double.parseDouble(yVal);
        double r = Double.parseDouble(rVal);
        if (r == 0) {
            return ((Boolean) false).toString();
        }
        if (r > 0) {
            if (x > 0 && y > 0) return ((Boolean) (x < r / 2 && y < r)).toString();
            if (x < 0 && y > 0) return ((Boolean) ((Math.pow(x, 2)) + (Math.pow(y, 2)) <= Math.pow(r, 2))).toString();
            if (x < 0 && y < 0) return ((Boolean) (y >= -r / 2 - x / 2)).toString();
        }
        if (r < 0) {
            if (x > 0 && y > 0) return ((Boolean) (y <= Math.abs(r / 2) - x / 2)).toString();
            if (x < 0 && y < 0) return ((Boolean) (y > r && x > r / 2)).toString();
            if (x > 0 && y < 0) return ((Boolean) (Math.pow(x, 2) + Math.pow(y, 2) <= Math.pow(r, 2))).toString();
        }
        return ((Boolean) false).toString();
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }
}





