<%-- 
    Document   : index
    Created on : Apr 28, 2008, 4:39:30 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="MacRoman"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=MacRoman">
        <title>Wonderland Art Upload</title>
    </head>
    <body>
        <h2>Art upload</h2>
        <form name="art" action="upload" method="POST" enctype="multipart/form-data">
            <table border="0">
                <tbody>
                    <tr>
                        <td>Name:</td>
                        <td><input type="text" name="name" size="24"></td>
                    </tr>
                    <tr>
                        <td>Location:</td>
                        <td>X: <input type="text" name="xloc" size="4">
                            Y: <input type="text" name="yloc" size="4">
                            Z: <input type="text" name="zloc" size="4"></td>
                    </tr>
                    <tr>
                        <td>Rotation:</td>
                        <td>X: <input type="text" name="xrot" size="4">
                            Y: <input type="text" name="yrot" size="4">
                            Z: <input type="text" name="zrot" size="4">
                            A: <input type="text" name="arot" size="4"></td>
                    </tr>
                    <tr>
                        <td>Bounds:</td>
                        <td>X: <input type="text" name="xbounds" size="4">
                            Y: <input type="text" name="ybounds" size="4">
                            Z: <input type="text" name="zbounds" size="4"></td>
                    </tr>
                    <tr>
                        <td>Model file (.j3s.gz):</td>
                        <td><input type="file" name="model" size="24"></td>
                    </tr>
                    <tr>
                        <td>Texture files (.zip):</td>
                        <td><input type="file" name="textures" size="24"></td>
                    </tr>
                    <tr>
                        <td></td>
                        <td><input type="submit"></td>
                    </tr>
                </tbody>
            </table>     
        </form>
    </body>
</html>
