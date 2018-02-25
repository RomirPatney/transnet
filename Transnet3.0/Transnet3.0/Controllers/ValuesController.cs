using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using System.Data;
using System.Data.SqlClient;
using System.Collections;

namespace Transnet3._0.Controllers
{
    [Route("api/[controller]")]
    public class ValuesController : ApiController
    {
        // ArrayList json = new ArrayList();
        [HttpPost]
        [Route("api/Values/Register")]
        public void Register([FromBody]registorInfo Info)
        {
            if (!ModelState.IsValid)
            {
                throw new Exception();
            }

            string queryString =
                "INSERT INTO API_transnet_Primary (email, passwor, imageurl, payment_no, payment_confirm, bought_today) VALUES( \'" + Info.Email + "\', \'" + Info.Password + "\', \'" + Info.ImageUrl + "\', \'" + Info.payment_no + "\' " +
                    ", " + Info.payment_confirm + ", " + Info.bought_today + " )";

            LOG_TO_DB(queryString);

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
              "SELECT email, passwor FROM API_transnet_Primary WHERE email = \'" + Info.Email + "\'";
            string test = Auth_(queryString).ToString();
            if (test == Info.Password.ToString())
                return true;
            else
                return false;

        }

        public bool Pay([FromBody]string value)
        {
            return false;
        }

        public bool Pay_confirm([FromBody]string value)
        {
            return false;
        }

        static void LOG_TO_DB(string queryString)
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
                }
                catch (Exception ex)
                {
                    throw new Exception();
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

        public class registorInfo
        {
            public string Email { get; set; }
            public string Password { get; set; }
            public string ImageUrl { get; set; }
            public string payment_no { get; set; }
            public int payment_confirm { get; set; }
            public int bought_today { get; set; }


        }
    }
}

