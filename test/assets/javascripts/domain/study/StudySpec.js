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

    var Study;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(extendedDomainEntities) {
      this.httpBackend        = this.$injector.get('$httpBackend');
      this.Study              = this.$injector.get('Study');
      this.StudyStatus        = this.$injector.get('StudyStatus');
      this.funutils           = this.$injector.get('funutils');
      this.jsonEntities       = this.$injector.get('jsonEntities');
      this.testUtils          = this.$injector.get('testUtils');

      this.testUtils.addCustomMatchers();

      Study = this.Study;

      this.jsonStudy = this.jsonEntities.study();
    }));

    afterEach(function() {
      this.httpBackend.verifyNoOutstandingExpectation();
      this.httpBackend.verifyNoOutstandingRequest();
    });

    it('constructor with no parameters has default values', function() {
      var study = new this.Study();

      expect(study.id).toBeNull();
      expect(study.version).toBe(0);
      expect(study.timeAdded).toBeNull();
      expect(study.timeModified).toBeNull();
      expect(study.name).toBeEmptyString();
      expect(study.description).toBeNull();
      expect(study.status).toBe(this.StudyStatus.DISABLED());
    });

    it('fails when creating from an invalid object', function() {
      var self = this,
          badStudyJson = _.omit(self.jsonEntities.study(), 'name');

      expect(function () { self.Study.create(badStudyJson); }).toThrowErrorOfType('Error');
    });

    it('fails when creating from a non object for an annotation type', function() {
      var self = this,
          badStudyJson = self.jsonEntities.study({ annotationTypes: [ 1 ]});
      expect(function () { self.Study.create(badStudyJson); }).toThrowErrorOfType('Error');
    });

    it('status predicates return valid results', function() {
      var self = this;
      _.each(self.StudyStatus.values(), function(status) {
        var study = new self.Study(self.jsonEntities.study({ status: status }));
        expect(study.isDisabled()).toBe(status === self.StudyStatus.DISABLED());
        expect(study.isEnabled()).toBe(status === self.StudyStatus.ENABLED());
        expect(study.isRetired()).toBe(status === self.StudyStatus.RETIRED());
      });
    });

    it('can retrieve a single study', function() {
      var self = this;
      self.httpBackend.whenGET(uri(this.jsonStudy.id)).respond(serverReply(this.jsonStudy));
      self.Study.get(this.jsonStudy.id).then(expectStudy).catch(failTest);
      self.httpBackend.flush();
    });

    it('fails when getting a study and it has a bad format', function() {
      var self = this,
          study = _.omit(self.jsonStudy, 'name');
      self.httpBackend.whenGET(uri(study.id)).respond(serverReply(study));

      self.Study.get(study.id).then(shouldNotFail).catch(shouldFail);
      self.httpBackend.flush();

      function shouldNotFail(reply) {
        fail('function should not be called');
      }

      function shouldFail(error) {
        expect(error).toStartWith('invalid object from server');
      }
    });

    it('fails when getting a study and it has a bad annotation type', function() {
      var self = this,
          annotationType = _.omit(self.jsonEntities.annotationType(), 'name'),
          study = self.jsonEntities.study({ annotationTypes: [ annotationType ]});

      self.httpBackend.whenGET(uri(study.id)).respond(serverReply(study));

      self.Study.get(study.id).then(shouldNotFail).catch(shouldFail);
      self.httpBackend.flush();

      function shouldNotFail(error) {
        fail('function should not be called: ' + error);
      }

      function shouldFail(error) {
        expect(error).toStartWith('invalid annotation types from server');
      }
    });

    it('can retrieve studies', function() {
      var self = this,
          studies = [ self.jsonEntities.study({ annotationTypes: [] }) ],
          reply = self.jsonEntities.pagedResult(studies);

      self.httpBackend.whenGET(uri()).respond(serverReply(reply));

      self.Study.list().then(testStudy).catch(failTest);
      self.httpBackend.flush();

      function testStudy(pagedResult) {
        expect(pagedResult.items).toBeArrayOfSize(1);
        expect(pagedResult.items[0]).toEqual(jasmine.any(self.Study));
        pagedResult.items[0].compareToJsonEntity(studies[0]);
      }
    });

    it('can list studies using options', function() {
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
        var studies = [ self.jsonStudy ],
            reply   = self.jsonEntities.pagedResult(studies),
            url     = sprintf.sprintf('%s?%s', uri(), $.param(options, true));

        self.httpBackend.whenGET(url).respond(serverReply(reply));

        self.Study.list(options).then(testStudy).catch(failTest);
        self.httpBackend.flush();

        function testStudy(pagedResult) {
          expect(pagedResult.items).toBeArrayOfSize(studies.length);
          _.each(pagedResult.items, function (study) {
            expect(study).toEqual(jasmine.any(self.Study));
          });
        }
      });
    });

    it('fails when list returns an invalid study', function() {
      var self = this,
          studies = [ _.omit(self.jsonStudy, 'name') ],
          reply = self.jsonEntities.pagedResult(studies);

      self.httpBackend.whenGET(uri()).respond(serverReply(reply));
      self.Study.list().then(listFail).catch(shouldFail);
      self.httpBackend.flush();

      function listFail(reply) {
        fail('function should not be called');
      }

      function shouldFail(error) {
        expect(error).toStartWith('invalid studies from server');
      }
    });

    it('can add a study', function() {
      var self = this,
          study = new self.Study(_.omit(this.jsonStudy, 'id')),
          json = _.pick(study, 'name', 'description');

      self.httpBackend.expectPOST(uri(), json).respond(201, serverReply(this.jsonStudy));

      study.add().then(expectStudy).catch(failTest);
      self.httpBackend.flush();
    });

    it('can update the name on a study', function() {
      var self  = this,
          study = new self.Study(this.jsonStudy);

      this.testUtils.updateEntity.call(this,
                                       study,
                                       'updateName',
                                       study.name,
                                       uri('name', study.id),
                                       { name: study.name },
                                       this.jsonStudy,
                                       expectStudy,
                                       failTest);
    });

    it('can update the description on a study', function() {
      var self = this,
          study = new self.Study(this.jsonStudy);

      this.testUtils.updateEntity.call(this,
                                       study,
                                       'updateDescription',
                                       undefined,
                                       uri('description', study.id),
                                       { },
                                       this.jsonStudy,
                                       expectStudy,
                                       failTest);

      this.testUtils.updateEntity.call(this,
                                       study,
                                       'updateDescription',
                                       study.description,
                                       uri('description', study.id),
                                       { description: study.description },
                                       this.jsonStudy,
                                       expectStudy,
                                       failTest);
    });

    it('can add an annotation type on a study', function() {
      var self = this,
          annotationType = self.jsonEntities.annotationType(),
          study          = new self.Study(this.jsonStudy);

      this.testUtils.updateEntity.call(this,
                                       study,
                                       'addAnnotationType',
                                       _.omit(annotationType, 'uniqueId'),
                                       uri('pannottype', study.id),
                                       _.omit(annotationType, 'uniqueId'),
                                       this.jsonStudy,
                                       expectStudy,
                                       failTest);
    });

    it('can remove an annotation on a study', function() {
      var self = this,
          annotationType = self.jsonEntities.annotationType(),
          baseStudy      = self.jsonEntities.study({ annotationTypes: [ annotationType ] }),
          study          = new self.Study(baseStudy),
          url            = sprintf.sprintf('%s/%d/%s', uri('pannottype', study.id),
                                           study.version, annotationType.uniqueId);

      self.httpBackend.whenDELETE(url).respond(201, serverReply(true));
      study.removeAnnotationType(annotationType).then(expectStudy).catch(failTest);
      self.httpBackend.flush();
    });

    it('can disable a study', function() {
      var jsonStudy = this.jsonEntities.study({ status: this.StudyStatus.ENABLED() });
      changeStatusShared.call(this, jsonStudy, 'disable', this.StudyStatus.DISABLED());
    });

    it('throws an error when disabling a study and it is already disabled', function() {
      var study = new this.Study(this.jsonEntities.study({ status: this.StudyStatus.DISABLED() }));
      expect(function () { study.disable(); })
        .toThrowErrorOfType('Error');
    });

    it('can enable a study', function() {
      var jsonStudy = this.jsonEntities.study({ status: this.StudyStatus.DISABLED() });
      changeStatusShared.call(this, jsonStudy, 'enable', this.StudyStatus.ENABLED());
    });

    it('throws an error when enabling a study and it is already enabled', function() {
      var study = new this.Study(this.jsonEntities.study({ status: this.StudyStatus.ENABLED() }));
      expect(function () { study.enable(); })
        .toThrowErrorOfType('Error');
    });

    it('can retire a study', function() {
      var jsonStudy = this.jsonEntities.study({ status: this.StudyStatus.DISABLED() });
      changeStatusShared.call(this, jsonStudy, 'retire', this.StudyStatus.RETIRED());
    });

    it('throws an error when retiring a study and it is already retired', function() {
      var study = new this.Study(this.jsonEntities.study({ status: this.StudyStatus.RETIRED() }));
      expect(function () { study.retire(); })
        .toThrowErrorOfType('Error');
    });

    it('can unretire a study', function() {
      var jsonStudy = this.jsonEntities.study({ status: this.StudyStatus.RETIRED() });
      changeStatusShared.call(this, jsonStudy, 'unretire', this.StudyStatus.DISABLED());
    });

    it('throws an error when unretiring a study and it is not retired', function() {
      var study = new this.Study(this.jsonEntities.study({ status: this.StudyStatus.DISABLED() }));
      expect(function () { study.unretire(); })
        .toThrowErrorOfType('Error');

      study = new this.Study(this.jsonEntities.study({ status: this.StudyStatus.ENABLED() }));
      expect(function () { study.unretire(); })
        .toThrowErrorOfType('Error');
    });

    function replyStudy(study, newValues) {
      newValues = newValues || {};
      return _.extend({}, study, newValues, {version: study.version + 1});
    }

    function serverReply(event) {
      return { status: 'success', data: event };
    }

    function changeStatusShared(jsonStudy, action, status) {
      /* jshint validthis:true */
      var self  = this,
          study = new self.Study(jsonStudy),
          json =  { expectedVersion: study.version },
          reply = replyStudy(jsonStudy, { status: status });

      self.httpBackend.expectPOST(uri(action, study.id), json).respond(201, serverReply(reply));
      expect(study[action]).toBeFunction();
      study[action]().then(checkStudy).catch(failTest);
      self.httpBackend.flush();

      function checkStudy(replyStudy) {
        expect(replyStudy).toEqual(jasmine.any(self.Study));
        expect(replyStudy.id).toEqual(study.id);
        expect(replyStudy.version).toEqual(study.version + 1);
        expect(replyStudy.status).toBe(status);
      }
    }

    // used by promise tests
    function expectStudy(entity) {
      expect(entity).toEqual(jasmine.any(Study));
    }

    // used by promise tests
    function failTest(error) {
      expect(error).toBeUndefined();
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
