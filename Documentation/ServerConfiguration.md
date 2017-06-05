# Server Configuration

digital-display-garden Uses three different configuration files.

They are `config.properties`, `config.properties.deployment`, and `authorized.users`.
These files should be made in `server/`. Note that those files are
in `.gitignore` and won't be committed.

The server treats the first argument you pass it as a path to the configuration
file. If no arguments are passed it simply tries to read from a file called
`config.properties` in the current working directory.

`authorized.users` is used to designate who has administrative privilege.
Admins can change what plants are visible and view collected visitor information.  

## Authorizing administrators

The `authorized.users` file looks like the following:

`AUTH` followed by a `google-affiliated mail`

``` 
ALLOW admin1@googleaffiliated.website.com
ALLOW admin2@google.com
ALLOW admin3@morris.umn.edu
ALLOW admin4@umn.edu
```
## Configuring Server

`config.properties` and `config.properties.deployment` are in the Properties file format
[(see this demo)](https://www.mkyong.com/java/java-properties-file-examples/)

The config files have the following fields:
##### serverPort

This is the port that the Spark server will run on: `2538`

##### clientID

This is the Google OAuth2.0 Client ID. It is essentially
a "username" for their API, but should be kept secret I believe.
See [GoogleAuthCredentials](./GoogleAuthCredentials.md) for how to 
create a client ID.

##### clientSecret

This is the Google OAuth2.0 Client Secret. This is essentially
a "password" for their API, and MUST be kept secret.
See [GoogleAuthCredentials](./GoogleAuthCredentials.md) for how to 
create a client secret.

##### publicURL

This is the URL that the server thinks visitors will be accessing
it from. It is important for this to be correct for security reasons,
and for a couple other things that assume we can create URLs for
visitors. During development on your local machine, this would be
something like `http://localhost:9000`, but in production, it would
look something like `https://a.real.website.com`. This must _not_
end in a slash.

##### callbackURL

This is the URL to which Google sends users after we have authenticated
them. During development, it should look like `http://localhost:2538/callback`.
During production, it should be the same domain port as `publicURL`, but
with `/callback` on the end.

##### databaseName

This is the name of the local MongoDB database that the program uses for
storing all its data.


## Example config.properties file

Here is an example of what such a file would look like:
```
# Make sure there is no trailing white space at the end of any lines
# Server Port
serverPort=2538
# Google OAuth2.0 Client ID
clientID=verylongstringofrandomnumbersandletters.apps.googleusercontent.com
# Google OAuth2.0 Client Secret
clientSecret=notquitesuchalongrandomstring
# The public URL of the website
publicURL=http://localhost:9000
# The callback URL of the website
callbackURL=http://localhost:2538/callback
# The Mongo database name
databaseName=test
```