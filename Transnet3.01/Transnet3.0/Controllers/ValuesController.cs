using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using System.Data;
using System.Data.SqlClient;
using System.Collections;
using System.Drawing;
using System.IO;

namespace Transnet3._0.Controllers
{
    [Route("api/[controller]")]
    public class ValuesController : ApiController
    {
        // ArrayList json = new ArrayList();
        [HttpPost]
        [Route("api/Values/Register")]
        public bool Register([FromBody]registorInfo Info)
        {
            if (!ModelState.IsValid)
            {
                throw new Exception();
            }
            Info.payment_confirm = 0;
            Info.bought_today = 0;
            string queryString =
                "INSERT INTO API_transnet_Primary (email, passwor, imageurl, payment_no, payment_confirm, bought_today) VALUES( \'" + Info.fromMonth + "\', \'" + Info.Password + "\', \'" + Info.ImageUrl + "\', \'" + Info.payment_no + "\' " +
                    ", " + Info.payment_confirm + ", " + Info.bought_today + " )";

            if(LOG_TO_DB(queryString))
            {
                return true;
            }
            else
            {
                return false;
            }

        }

        [HttpGet]
        [Route("api/test")]
        public string Test()
        {
            return "it's lit bro";

        }

        [HttpPost]
        [Route("api/Values/Login")]
        public bool Login([FromBody]registorInfo Info)
        {
            string queryString =
              "SELECT email, passwor FROM API_transnet_Primary WHERE email = \'" + Info.fromMonth + "\'";
            string test = Auth_(queryString).ToString();
            if (test == Info.Password.ToString())
                return true;
            else
                return false;

        }

        [HttpPost]
        [Route("api/Values/Pay")]
        public bool Pay([FromBody]registorInfo Info)
        {
            if (!ModelState.IsValid)
            {
                throw new Exception();
            }
            Info.payment_confirm = 0;
            Info.bought_today = 1;
            string queryString =
                "INSERT INTO API_transnet_Secondary (email, passwor, imageurl, payment_no, payment_confirm, bought_today) VALUES( \'" + Info.fromMonth + "\', \'" + Info.Password + "\', \'" + Info.ImageUrl + "\', \'" + Info.payment_no + "\' " +
                    ", " + Info.payment_confirm + ", " + Info.bought_today + " )";

            if (LOG_TO_DB(queryString))
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        public bool Pay_confirm(string stationUrl)
        {
            //planning to add user name 
            return false;
            //string queryString =
            //    "SELECT imageurl from API_transnet_Secondary";
           
            //string connectionString = "Server=tcp:transnetserver.database.windows.net,1433;Initial Catalog=API_transnet;Persist Security Info=False;User ID=admin_server;Password=romir123123@;MultipleActiveResultSets=False;Encrypt=True;TrustServerCertificate=False;Connection Timeout=30;";

            //string image_link = "";
            //using (SqlConnection connection =
            //    new SqlConnection(connectionString))
            //{
            //    // Create the Command and Parameter objects.
            //    SqlCommand command = new SqlCommand(queryString, connection);
            //    try
            //    {
            //        connection.Open();
            //        SqlDataReader reader = command.ExecuteReader();
            //        int x = 0;
            //        while (reader.Read())
            //        {
            //            image_link = reader[0].ToString();
            //            byte[] binaryUrl = StrToByteArray(image_link);
            //            Image imageUrl = CreateImage(binaryUrl);
            //            imageUrl.Save("C:\\Users\\romir\\Documents\\Visual Studio 2017\\Projects\\Transnetwork\\Secondary_DB_images\\THEBUYER" + x+".bmp");

            //            //NOW WE SHOULD COMPARE BETWEEN THE IMAGE URL (PIC FROM APP) AND STAION URL (PIC FROM STATION) USING AZURE API

            //            //MakeAnalysisRequest("C:\\Users\\romir\\Documents\\Visual Studio 2017\\Windows\\VideoFrameAnalyzer\\THEBUYER.bmp");
            //            //MakeAnalysisRequest(stationUrl);



            //            x++;
            //        }
            //        reader.Close();
            //        return false;
                    
            //    }
            //    catch (Exception ex)
            //    {
            //        return false;
            //    }
            //}
            
        }




        //  convert a string to a byte array. 
        public static byte[] StrToByteArray(string str)
        {
            System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();
            return encoding.GetBytes(str);
        }

        //convert a binary list to image
        public static Image CreateImage(byte[] imageData)
        {
            Image image;
            using (MemoryStream inStream = new MemoryStream())
            {
                inStream.Write(imageData, 0, imageData.Length);

                image = Bitmap.FromStream(inStream);
            }

            return image;
        }

        static bool LOG_TO_DB(string queryString)
        {
            string connectionString = "Server=tcp:transnetserver.database.windows.net,1433;Initial Catalog=API_transnet;Persist Security Info=False;User ID=admin_server;Password=romir123123@;MultipleActiveResultSets=False;Encrypt=True;TrustServerCertificate=False;Connection Timeout=30;";
            //"Server=tcp:transnetserver.database.windows.net,1433;Initial Catalog=API_transnet;Persist Security Info=False;User ID=admin_server;MultipleActiveResultSets=False;Encrypt=True;TrustServerCertificate=False;Connection Timeout=30;"

            // Create and open the connection in a using block. This
            // ensures that all resources will be closed and disposed
            // when the code exits.
            using (SqlConnection connection =
                new SqlConnection(connectionString))
            {
                // Create the Command and Parameter objects.
                SqlCommand command = new SqlCommand(queryString, connection);

                try
                {
                    connection.Open();
                    command.ExecuteNonQuery();
                    return true;
                }
                catch (Exception ex)
                {
                    return false;
                }

            }

        }

        static string Auth_(string queryString)
        {
            string connectionString = "Server=tcp:transnetserver.database.windows.net,1433;Initial Catalog=API_transnet;Persist Security Info=False;User ID=admin_server;Password=romir123123@;MultipleActiveResultSets=False;Encrypt=True;TrustServerCertificate=False;Connection Timeout=30;";

            string password = "";
            using (SqlConnection connection =
                new SqlConnection(connectionString))
            {
                // Create the Command and Parameter objects.
                SqlCommand command = new SqlCommand(queryString, connection);
                try
                {
                    connection.Open();
                    SqlDataReader reader = command.ExecuteReader();
                    while (reader.Read())
                    {
                        password = reader[1].ToString();
                    }
                    reader.Close();

                    //if (email == "" || email == null || password == "" || password == null)
                      //  return false;

                    return password;
                }
                catch (Exception ex)
                {
                    return "";
                }
            }


        }

        //third party

        [HttpPost]
        [Route("api/Values/ThirdPartyPost")]
        public bool ThirdPartyPost([FromBody]ThirdPartyInfo Info)
        {
            if (!ModelState.IsValid)
            {
                throw new Exception();
            }

            string queryString =
                "INSERT INTO Third_Party_ (fromMonth, fromDay, fromHour, toMonth, toDay, toHour, lat1, lon1, lat2, lon2, Mode ) VALUES( \'" + Info.fromMonth + "\', \'" + Info.fromDay + "\', \'" + Info.fromHour + "\', \'" + Info.toMonth + "\' " +
                    ", \'" + Info.toDay + "\', \'" + Info.toHour + "\', \'"+ Info.lat1 +"\', \'" + Info.lon1 + "\', \'" + Info.lat2 + "\', \'" + Info.lon2 +"\', \'"+ Info.Mode+ "\')";

            if (LOG_TO_DB(queryString))
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        [HttpPost]
        [Route("api/Values/ThirdPartyGet")]
        public string ThirdPartyGet([FromBody]ThirdPartyInfoget Info)
        {
            string queryString =
              "SELECT * FROM Third_Party_ WHERE fromMonth =\'" + Info.fromMonth + "\', fromDay= \'" + Info.fromDay + "\', fromHour= \'" + Info.fromHour + "\', lat1= \'" + Info.lat1 + "\', lon1= \'" + Info.lon1 + "\', lat2= \'" + Info.lat2 + "\', lon2= \'" + Info.lon2 + "\')";

            double lat1 =0;
            double lon1 =0;
            double lat2 =0;
            double lon2 =0;
            int mode = 0;

            string connectionString = "Server=tcp:transnetserver.database.windows.net,1433;Initial Catalog=API_transnet;Persist Security Info=False;User ID=admin_server;Password=romir123123@;MultipleActiveResultSets=False;Encrypt=True;TrustServerCertificate=False;Connection Timeout=30;";

            using (SqlConnection connection =
                new SqlConnection(connectionString))
            {
                SqlCommand command = new SqlCommand(queryString, connection);
                try
                {
                    connection.Open();
                    SqlDataReader reader = command.ExecuteReader();
                    while (reader.Read())
                    {
                        lat1= Convert.ToDouble(reader[6]);
                        lon1= Convert.ToDouble(reader[7]);
                        lat2 = Convert.ToDouble(reader[8]);
                        lon2 = Convert.ToDouble(reader[9]);
                        mode = Convert.ToInt32(reader[10]);

                        double dist = distance(lat1, lon1, lat2, lon2);
                        if (dist > 20)
                        {
                            return lat1 + " " + lat2 + " " + lon1 + " " + lon2 + " " + mode;
                        }

                    }

                    
                    
                    reader.Close();
                    return "";
                }
                catch (Exception ex)
                {
                    return "";
                }
            }
        }

        private double distance(double lat1, double lon1, double lat2, double lon2)
        {
            double theta = lon1 - lon2;
            double dist = Math.Sin(deg2rad(lat1)) * Math.Sin(deg2rad(lat2)) + Math.Cos(deg2rad(lat1)) * Math.Cos(deg2rad(lat2)) * Math.Cos(deg2rad(theta));
            dist = Math.Acos(dist);
            dist = rad2deg(dist);
            dist = dist * 60 * 1.1515;
            return (dist);
        }

        private double deg2rad(double deg)
        {
            return (deg * Math.PI / 180.0);
        }
        private double rad2deg(double rad)
        {
            return (rad * 180.0 / Math.PI);
        }


        public class registorInfo
        {
            public string fromMonth { get; set; }
            public string Password { get; set; }
            public string ImageUrl { get; set; }
            public string payment_no { get; set; }
            public int payment_confirm { get; set; }
            public int bought_today { get; set; }
        }
        public class ThirdPartyInfo
        {
            public string fromMonth { get; set; }
            public string fromDay { get; set; }
            public string fromHour { get; set; }
            public string toMonth { get; set; }
            public string toDay { get; set; }
            public string toHour { get; set; }
            public string lat1 { get; set; }
            public string lon1 { get; set; }
            public string lat2 { get; set; }
            public string lon2 { get; set; }
            public string Mode { get; set; }
        }

        public class ThirdPartyInfoget
        {
            public string fromMonth { get; set; }
            public string fromDay { get; set; }
            public string fromHour { get; set; }
            public string lat1 { get; set; }
            public string lon1 { get; set; }
            public string lat2 { get; set; }
            public string lon2 { get; set; }
        }
    }
}

