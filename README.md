# Skills

Skills is a Skill Management Tool for small and medium enterprises.
It runs as a Plugin Module in the Doubleclue Enterprise Management ( **DCEM** ). 
You can find the DCEM repository at https://github.com/HWS-DoubleClue/IAM-Password-Manager. 

For further details, have a look at the [Skills Manual](https://doubleclue.com/files/DC_Skills_Manual_en.pdf)


## Features 

### Skill Hierarchy
Skills are arranged in a hierarchy by assigning child skills to a parent. This allows you a better overview over the skills in your company and to define skills as finegrained as you want.

### Skill Levels
When assigning a skill to a user, you can select one of four skill levels to show the users experience with this skill: Basic, Normal, Advanced or Expert.

### Skill Targets
In addition to mananging the skills a user already owns, Skills also supports talent management by allowing users and their superiors to define skill targets a user shall learn. 

### Certificates
In addition to skills, users can also manage their certificates in Skills - adding the certificates they have earned or want to earn to their profile. For certificates that needs to be renewed regularly, an expiration date can be added and the users will receive an automatic e-mail notification when a certificate that is about to expire.  

### Skill Profiles
To better match users with positions that require a certain skill set, you can define Skill Profiles. Skill Profiles are a collection of selected skills and certificates a user needs to fullfill this role. You can then assign the profile to a user and it will automtically calculate a match value, which shows how well the user fits the skill profile.

### Detailed User Search
Skills comes with a detailed user search, enabling you to look for users with certain skills, certificates or skill profiles in your enterprise. You can further restricht the results to certain skill levels or skill profiles with a defined match value.


## [Try it out for free](https://doubleclue.online/dcem/createTenant/index.xhtml)

### On Premises or in the Cloud

You can install the solution in a DCEM on premises or as a "Software As A Service" in our DoubleClue cloud.

### Build

You can download the latest release version of the Skills module from GitHub.

If you prefer to build a snapshot version yourself, follow these steps:
 
- Check out DCEM at https://github.com/HWS-DoubleClue/IAM-Password-Manager
- Check out the Skills-Module
- Build DCEM in an IDE of your choice
- Execute Maven with clean package
- The output is a jar file in the target folder


#### Install on Premises
The installation of the Skills-Module is quick and easy. Simply copy the SkillsModule.jar to the subfolder "plugins" in your DCEM folder and restart DCEM.
You would also need to acquire a license key from the Doubleclue support.  
- Contact: support@doubleclue.com

For more information, check our detailed instruction at https://github.com/HWS-DoubleClue/IAM-Password-Manager/blob/master/Documents/InstallPluginModule.odt or have a look at the [DoubleClue Manual](https://doubleclue.com/wp-content/uploads/DCEM_Manual_EN.pdf) Chapter 20: DoubleClue Plugin Modules.



