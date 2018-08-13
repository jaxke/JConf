# JConf  
Simple configuration library with no dependencies.  

## Features  
* Automatic type conversion when possible
* Configuration can be manipulated at runtime with put() method  
* Automatic backup of configuration files  

## Usage  
Initialization:  
```
String confFile = "config/conf.jc";  
Jconf jc = new Jconf(confFile);  
```
  
The API exposes methods: get(), getVal() and set(). Getter returns the configuration in the form of
```
HashMap<String, HashMap<String, Object>>  
```
Where the key is a category name for a group of settings and the inner HashMap stores the keys and values of the entries
under the category.  
For example if this is the configuration file:  
```
{General}  
Active = true  
Limit = 1.8  
Bandwidth = 100M  
  
{Not so general}  
Balance = 500  
// Note the format of the list(also, this is a valid comment inside a config file)  
Users = [Hamilton, Webber, Raikkonen]  
```
jc.get() returns the entire configuration HashMap:  
```
[{Not so general={Users=[Ljava.lang.String;@4b67cf4d, Balance=500}, General={Active=true, Bandwidth=100M, Limit=1.8}}]
// The array under "Users" is simply a String array of the listed users
```
The getVal() method returns the object value of the requested key:
```
jc.getVal("General", "Limit);
>> 1.8
```
  
The set() method allows the programmer to change values of settings during runtime.  
```
jc.set(String category, String element, String value)  
```
  
Supported type conversions:  
* "true" || "True" -> boolean true  
* "false" || "False" -> boolean false  
* "1.1" -> Double 1.1
* "1" -> int 1  
* [i1, i2, i3] -> Object array[] = {i1, i2, i3}  
Type will be String if the value could not be converted.