/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'jquery',
  'underscore',
  'sprintf',
  'biobankApp'
], function(angular, mocks, $, _, sprintf) {
  'use strict';

  /**
   * For now these tests test the interaction between the class and the server.
   *
   * At the moment not sure if we need the service layer, or if the domain model objects call the rest API
   * directly. If the service layer is kept then these tests will have to be modified and only mock the
   * service methods in 'studiesService'.
   */
  describe('Study', function() {

    // used by promise tests
    function failTest(error) {
      expect(error).toBeUndefined();
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(extendedDomainEntities) {
      this.httpBackend        = this.$injector.get('$httpBackend');
      this.Study              = this.$injector.get('Study');
      this.StudyStatus        = this.$injector.get('StudyStatus');
      this.funutils           = this.$injector.get('funutils');
      this.fakeEntities       = this.$injector.get('fakeDomainEntities');
      this.testUtils          = this.$injector.get('testUtils');
    }));

    it('constructor with no parameters has default values', function() {
      var self = this,
          study = new self.Study();

      expect(study.id).toBeNull();
      expect(study.version).toBe(0);
      expect(study.timeAdded).toBeNull();
      expect(study.timeModified).toBeNull();
      expect(study.name).toBeEmptyString();
      expect(study.description).toBeNull();
      expect(study.status).toBe(self.StudyStatus.DISABLED());
    });

    it('fails when creating from an invalid object', function() {
      var self = this,
          badStudyJson = _.omit(self.fakeEntities.study(), 'name');

      expect(function () { self.Study.create(badStudyJson); }).toThrowErrorOfType('Error');
    });

    it('fails when creating from a non object for an annotation type', function() {
      var self = this,
          badStudyJson = self.fakeEntities.study({ annotationTypes: [ 1 ]});
      expect(function () { self.Study.create(1); }).toThrowErrorOfType('Error');
    });

    it('status predicates return valid results', function() {
      var self = this;
      _.each(self.StudyStatus.values(), function(status) {
        var study = new self.Study(self.fakeEntities.study({ status: status }));
        expect(study.isDisabled()).toBe(status === self.StudyStatus.DISABLED());
        expect(study.isEnabled()).toBe(status === self.StudyStatus.ENABLED());
        expect(study.isRetired()).toBe(status === self.StudyStatus.RETIRED());
      });
    });

    it('can retrieve a single study', function(done) {
      var self = this,
          study = self.fakeEntities.study();
      self.httpBackend.whenGET(uri(study.id)).respond(serverReply(study));

      self.Study.get(study.id).then(checkResult).catch(failTest).finally(done);
      self.httpBackend.flush();

      function checkResult(reply) {
        expect(reply).toEqual(jasmine.any(self.Study));
        reply.compareToServerEntity(study);
        done();
      }
    });

    it('fails when getting a study and it has a bad format', function(done) {
      var self = this,
          study = _.omit(self.fakeEntities.study(), 'name');
      self.httpBackend.whenGET(uri(study.id)).respond(serverReply(study));

      self.Study.get(study.id).then(shouldNotFail).catch(shouldFail).finally(done);
      self.httpBackend.flush();

      function shouldNotFail(reply) {
        fail('function should not be called');
      }

      function shouldFail(error) {
        expect(error).toStartWith('invalid object from server');
      }
    });

    it('fails when getting a study and it has a bad annotation type', function(done) {
      var self = this,
          annotationType = self.fakeEntities.annotationType(),
          study = self.fakeEntities.study({ annotationTypes: [ annotationType ]});

      self.httpBackend.whenGET(uri(study.id)).respond(serverReply(study));

      self.Study.get(study.id).then(shouldNotFail).catch(shouldFail).finally(done);
      self.httpBackend.flush();

      function shouldNotFail(reply) {
        fail('function should not be called');
      }

      function shouldFail(error) {
        expect(error).toStartWith('invalid annotation types from server');
      }
    });

    it('can retrieve studies', function(done) {
      var self = this,
          studies = [ self.fakeEntities.study({ annotationTypes: [] }) ],
          reply = self.fakeEntities.pagedResult(studies),
          serverEntity;

      self.httpBackend.whenGET(uri()).respond(serverReply(reply));

      self.Study.list().then(testStudy).catch(failTest).finally(done);
      self.httpBackend.flush();

      function testStudy(pagedResult) {
        expect(pagedResult.items).toBeArrayOfSize(studies.length);

        _.each(pagedResult.items, function (study) {
          expect(study).toEqual(jasmine.any(self.Study));

          serverEntity = _.findWhere(studies, { id: study.id });
          expect(serverEntity).toBeDefined();
          study.compareToServerEntity(serverEntity);
        });
      }
    });

    it('can list studies using options', function(done) {
      var self = this,
          optionList = [
            { filter: 'name' },
            { status: 'DisabledStudy' },
            { sort: 'status' },
            { page: 2 },
            { pageSize: 10 },
            { order: 'desc' }
          ];

      _.each(optionList, function (options) {
        var studies = [ self.fakeEntities.study() ],
            reply   = self.fakeEntities.pagedResult(studies),
            url     = sprintf.sprintf('%s?%s', uri(), $.param(options, true));

        self.httpBackend.whenGET(url).respond(serverReply(reply));

        self.Study.list(options).then(testStudy).catch(failTest);
        self.httpBackend.flush();

        function testStudy(pagedResult) {
          expect(pagedResult.items).toBeArrayOfSize(studies.length);
          _.each(pagedResult.items, function (study) {
            expect(study).toEqual(jasmine.any(self.Study));
          });

          if (options.order) {
            done();
          }
        }
      });
    });

    it('fails when list returns an invalid study', function(done) {
      var self = this,
          studies = [ _.omit(self.fakeEntities.study(), 'name') ],
          reply = self.fakeEntities.pagedResult(studies);

      self.httpBackend.whenGET(uri()).respond(serverReply(reply));

      self.Study.list().then(listFail).catch(shouldFail).finally(done);
      self.httpBackend.flush();

      function listFail(reply) {
        fail('function should not be called');
      }

      function shouldFail(error) {
        expect(error).toStartWith('invalid studies from server');
      }
    });

    it('can add a study', function(done) {
      var self = this,
          serverStudy = self.fakeEntities.study(),
          study = new self.Study(_.omit(serverStudy, 'id')),
          json = _.pick(study, 'name', 'description');

      self.httpBackend.expectPOST(uri(), json).respond(201, serverReply(serverStudy));

      study.add().then(testStudy).catch(failTest).finally(done);
      self.httpBackend.flush();

      function testStudy(replyStudy) {
        expect(replyStudy).toEqual(jasmine.any(self.Study));
        expect(replyStudy.id).toEqual(serverStudy.id);
        expect(replyStudy.version).toEqual(0);
        expect(replyStudy.name).toEqual(serverStudy.name);
        expect(replyStudy.description).toEqual(serverStudy.description);
      }
    });

    it('can update the name on a study', function(done) {
      var self = this,
          newName   = self.fakeEntities.stringNext(),
          baseStudy = self.fakeEntities.study(),
          study     = new self.Study(baseStudy),
          reply     = replyStudy(baseStudy, { name: newName }),
          json      = _.extend({ name: newName }, self.testUtils.expectedVersion(study.version));

      self.httpBackend.expectPOST(uri('name', study.id), json).respond(201, serverReply(reply));

      study.updateName(newName).then(testStudy).catch(failTest).finally(done);
      self.httpBackend.flush();

      function testStudy(replyStudy) {
        expect(replyStudy).toEqual(jasmine.any(self.Study));
        expect(replyStudy.id).toEqual(study.id);
        expect(replyStudy.version).toEqual(study.version + 1);
        expect(replyStudy.name).toEqual(newName);
        expect(replyStudy.description).toEqual(study.description);
        done();
      }
    });

    it('can update the description on a study', function(done) {
      var self = this;

      _.each([null, 'dont-care'], function (description) {
        var baseStudy = self.fakeEntities.study(),
            study     = new self.Study(baseStudy),
            reply     = replyStudy(baseStudy, { description: description }),
            json      = self.testUtils.expectedVersion(study.version);

        if (description === null) {
          reply = _.omit(reply, 'description');
        }

        self.httpBackend.expectPOST(uri('description', study.id), json).respond(201, serverReply(reply));
        study.updateDescription(description).then(testStudy).catch(failTest).finally(done);
        self.httpBackend.flush();

        function testStudy(replyStudy) {
          expect(replyStudy).toEqual(jasmine.any(self.Study));
          expect(replyStudy.id).toEqual(study.id);
          expect(replyStudy.version).toEqual(study.version + 1);
          expect(replyStudy.name).toEqual(study.name);
          expect(replyStudy.description).toEqual(description);
        }
      });
    });

    it('can add an annotation on a study', function(done) {
      var self = this,
          baseStudy      = self.fakeEntities.study(),
          study          = new self.Study(baseStudy),
          annotationType = self.fakeEntities.annotationType(),
          reply          = replyStudy(baseStudy, { annotationTypes: [ annotationType ]}),
          json           = _.extend(self.testUtils.expectedVersion(study.version),
                                    _.omit(annotationType, 'uniqueId'));

      self.httpBackend.expectPOST(uri('pannottype', study.id), json).respond(201, serverReply(reply));
      study.addAnnotationType(annotationType).then(testStudy).catch(failTest).finally(done);
      self.httpBackend.flush();

      function testStudy(replyStudy) {
        expect(replyStudy).toEqual(jasmine.any(self.Study));
        expect(replyStudy.id).toEqual(study.id);
        expect(replyStudy.version).toEqual(study.version + 1);
        expect(replyStudy.name).toEqual(study.name);
        expect(replyStudy.description).toEqual(study.description);
        expect(replyStudy.annotationTypes).toBeArrayOfSize(1);
        replyStudy.annotationTypes[0].compareToServerEntity(annotationType);
      }
    });

    it('can remove an annotation on a study', function(done) {
      var self = this,
          annotationType = self.fakeEntities.annotationType(),
          baseStudy      = self.fakeEntities.study({ annotationTypes: [ annotationType ] }),
          study          = new self.Study(baseStudy),
          url            = sprintf.sprintf('%s/%d/%s', uri('pannottype', study.id),
                                           study.version, annotationType.uniqueId);

      self.httpBackend.whenDELETE(url).respond(201, serverReply(true));

      study.removeAnnotationType(annotationType).then(testStudy).catch(failTest).finally(done);
      self.httpBackend.flush();

      function testStudy(replyStudy) {
        expect(replyStudy).toEqual(jasmine.any(self.Study));
        expect(replyStudy.id).toEqual(study.id);
        expect(replyStudy.version).toEqual(baseStudy.version + 1);
        expect(replyStudy.name).toEqual(study.name);
        expect(replyStudy.description).toEqual(study.description);
        expect(replyStudy.annotationTypes).toBeEmptyArray();
        done();
      }
    });

    it('can disable a study', function(done) {
      changeStatusShared.call(this, done, 'disable', this.StudyStatus.DISABLED());
    });

    it('can enable a study', function(done) {
      changeStatusShared.call(this, done, 'enable', this.StudyStatus.ENABLED());
    });

    it('can retire a study', function(done) {
      changeStatusShared.call(this, done, 'retire', this.StudyStatus.RETIRED());
    });

    it('can unretire a study', function(done) {
      var StudyStatus = this.$injector.get('StudyStatus');
      changeStatusShared.call(this, done, 'unretire', StudyStatus.DISABLED());
    });

    function replyStudy(study, newValues) {
      newValues = newValues || {};
      return _.extend({}, study, newValues, {version: study.version + 1});
    }

    function serverReply(event) {
      return { status: 'success', data: event };
    }

    function changeStatusShared(done, action, status) {
      /* jshint validthis:true */
      var self  = this,
          baseStudy = self.fakeEntities.study(),
          study = new self.Study(baseStudy),
          json =  { expectedVersion: study.version },
          reply = replyStudy(baseStudy, { status: status });

      self.httpBackend.expectPOST(uri(action, study.id), json).respond(201, serverReply(reply));
      expect(study[action]).toBeFunction();
      study[action]().then(checkStudy).catch(failTest).finally(done);
      self.httpBackend.flush();

      function checkStudy(replyStudy) {
        expect(replyStudy.id).toEqual(study.id);
        expect(replyStudy.version).toEqual(study.version + 1);
        expect(replyStudy.status).toBe(status);
      }
    }

    function uri(/* path, studyId */) {
      var args = _.toArray(arguments),
          studyId,
          path;

      var result = '/studies';

      if (args.length > 0) {
        path = args.shift();
        result += '/' + path;
      }

      if (args.length > 0) {
        studyId = args.shift();
        result += '/' + studyId;
      }

      return result;
    }

  });

});
/* Local Variables:  */
/* mode: js          */
/* End:              */
