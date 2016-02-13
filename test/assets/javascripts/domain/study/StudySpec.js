/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  /**
   * For now these tests test the interaction between the class and the server.
   *
   * At the moment not sure if we need the service layer, or if the domain model objects call the rest API
   * directly. If the service layer is kept then these tests will have to be modified and only mock the
   * service methods in 'studiesService'.
   */
  describe('Study', function() {

    var httpBackend, Study, StudyStatus, funutils, testUtils, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(extendedDomainEntities) {
      httpBackend  = this.$injector.get('$httpBackend');
      Study        = this.$injector.get('Study');
      StudyStatus  = this.$injector.get('StudyStatus');
      funutils     = this.$injector.get('funutils');
      fakeEntities = this.$injector.get('fakeDomainEntities');
      testUtils    = this.$injector.get('testUtils');
    }));

    it('constructor with no parameters has default values', function() {
      var study = new Study();

      expect(study.id).toBeNull();
      expect(study.version).toBe(0);
      expect(study.timeAdded).toBeNull();
      expect(study.timeModified).toBeNull();
      expect(study.name).toBeEmptyString();
      expect(study.description).toBeNull();
      expect(study.status).toBe(StudyStatus.DISABLED());
    });

    it('fails when creating from a non object', function() {
      expect(Study.create(1))
        .toEqual(new Error('invalid object from server: must be a map, has the correct keys'));
    });

    it('can retrieve a single study', function(done) {
      var study = fakeEntities.study();
      httpBackend.whenGET(uri(study.id)).respond(serverReply(study));

      Study.get(study.id).then(function (reply) {
        expect(reply).toEqual(jasmine.any(Study));
        reply.compareToServerEntity(study);
        done();
      });
      httpBackend.flush();
    });

    it('can retrieve studies', function(done) {
      var studies = [ fakeEntities.study() ],
          reply = fakeEntities.pagedResult(studies),
          serverEntity;

      httpBackend.whenGET(uri()).respond(serverReply(reply));

      Study.list().then(function (pagedResult) {
        expect(pagedResult.items).toBeArrayOfSize(studies.length);

        _.each(pagedResult.items, function (study) {
          expect(study).toEqual(jasmine.any(Study));

          serverEntity = _.findWhere(studies, { id: study.id });
          expect(serverEntity).toBeDefined();
          study.compareToServerEntity(serverEntity);
        });
        done();
      });
      httpBackend.flush();
    });

    it('can add a study', function(done) {
      var baseStudy = fakeEntities.study();
      var study = new Study(_.omit(baseStudy, 'id'));
      var cmd = addCommand(study);

      httpBackend.expectPOST(uri(), cmd).respond(201, serverReply(baseStudy));

      study.addOrUpdate().then(function(replyStudy) {
        expect(replyStudy.id).toEqual(baseStudy.id);
        expect(replyStudy.version).toEqual(0);
        expect(replyStudy.name).toEqual(study.name);
        expect(replyStudy.description).toEqual(study.description);
        done();
      });
      httpBackend.flush();
    });

    it('can update a study', function(done) {
      var baseStudy = fakeEntities.study();
      var study = new Study(baseStudy);
      var command = updateCommand(study);
      var reply = replyStudy(baseStudy);

      httpBackend.expectPUT(uri(study.id), command).respond(201, serverReply(reply));

      study.addOrUpdate().then(function(replyStudy) {
        expect(replyStudy.id).toEqual(study.id);
        expect(replyStudy.version).toEqual(study.version + 1);
        expect(replyStudy.name).toEqual(study.name);
        expect(replyStudy.description).toEqual(study.description);
        done();
      });
      httpBackend.flush();
    });

    it('can disable a study', function() {
      var StudyStatus = this.$injector.get('StudyStatus');
      changeStatusShared('disable', StudyStatus.DISABLED());
    });

    it('can enable a study', function() {
      var StudyStatus = this.$injector.get('StudyStatus');
      changeStatusShared('enable', StudyStatus.ENABLED());
    });

    it('can retire a study', function() {
      var StudyStatus = this.$injector.get('StudyStatus');
      changeStatusShared('retire', StudyStatus.RETIRED());
    });

    it('can unretire a study', function() {
      var StudyStatus = this.$injector.get('StudyStatus');
      changeStatusShared('unretire', StudyStatus.DISABLED());
    });

    function addCommand(study) {
      return  _.pick(study, 'name', 'description');
    }

    function updateCommand(study) {
      return _.extend(_.pick(study, 'id', 'name', 'description'),
                      testUtils.expectedVersion(study.version));
    }

    function changeStatusCommand(study) {
      return _.extend(_.pick(study, 'id'),
                      testUtils.expectedVersion(study.version));
    }

    function replyStudy(study, newValues) {
      newValues = newValues || {};
      return new Study(_.extend({}, study, newValues, {version: study.version + 1}));
    }

    function serverReply(event) {
      return { status: 'success', data: event };
    }

    function changeStatusShared(action, status) {
      var baseStudy = fakeEntities.study();
      var study = new Study(baseStudy);
      var command = changeStatusCommand(study);
      var reply = replyStudy(baseStudy, { status: status });


      httpBackend.expectPOST(uri(study.id) + '/' + action, command).respond(201, serverReply(reply));

      study[action]().then(function(replyStudy) {
        expect(replyStudy.id).toEqual(study.id);
        expect(replyStudy.version).toEqual(study.version + 1);
        expect(replyStudy.status).toBe(status);
      });
      httpBackend.flush();
    }

    function uri(studyId) {
      var result = '/studies';
      if (arguments.length > 0) {
        result += '/' + studyId;
      }
      return result;
    }

  });

});
/* Local Variables:  */
/* mode: js          */
/* End:              */
