# Notes

- Should this be implemented for ID generation: [Embedding a Primary Key in a UUID / GUID for CQRS / ES]
  (http://rbtech.blogspot.ca/2013/10/embedding-primary-key-in-uuid-guid-for.html)

# Using REST

Install this: https://github.com/jakubroztocil/httpie

```bash
http POST localhost:9000/studies X-XSRF-TOKEN:3d590b22-5cce-4f6a-9f63-0a4fcfc69103 type=AddStudyCmd name=ST1 description="Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam"

http POST localhost:9000/studies/pannottype X-XSRF-TOKEN:f53d509d-4768-48d5-a9f7-911c0dc4e051 type=AddParticipantAnnotationTypeCmd studyId=8B505F3E-88E9-42EA-AF4A-7003BD257390 name=PAT1 description="Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam" valueType=Select maxValueCount:=1 options:='{ "1": "1", "2": "2" }' required=true
```

# Learning AngularjS

https://github.com/jmcunningham/AngularJS-Learning

# Grids or Tables in AngularJS

http://stackoverflow.com/questions/21375073/best-way-to-represent-a-grid-or-table-in-angularjs-with-bootstrap-3

---

[Back to top](../README.md)
