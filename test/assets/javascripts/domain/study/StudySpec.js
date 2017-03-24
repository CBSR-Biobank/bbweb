/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks   = require('angularMocks'),
      _       = require('lodash'),
      sprintf = require('sprintf-js').sprintf;

  function SuiteMixinFactory(EntityTestSuiteMixin, ServerReplyMixin) {

    function SuiteMixin() {
      EntityTestSuiteMixin.call(this);
      ServerReplyMixin.call(this);
    }

    SuiteMixin.prototype = Object.create(EntityTestSuiteMixin.prototype);
    _.extend(SuiteMixin.prototype, ServerReplyMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    // used by promise tests
    SuiteMixin.prototype.failTest = function (error) {
      expect(error).toBeUndefined();
    };

    SuiteMixin.prototype.uri = function(/* path, studyId */) {
      var args = _.toArray(arguments),
          studyId,
          path;

      var result = '/studies/';

      if (args.length > 0) {
        path = args.shift();
        result += path;
      }

      if (args.length > 0) {
        studyId = args.shift();
        result += '/' + studyId;
      }

      return result;
    };

    return SuiteMixin;
  }

  describe('Study', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(EntityTestSuiteMixin,
                               ServerReplyMixin,
                               testDomainEntities) {
      var self = this,
          SuiteMixin = new SuiteMixinFactory(EntityTestSuiteMixin, ServerReplyMixin);

      _.extend(self, SuiteMixin.prototype);

      self.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'Study',
                              'StudyState',
                              'funutils',
                              'factory',
                              'testUtils');

      self.testUtils.addCustomMatchers();
      self.jsonStudy = self.factory.study();
      self.expectStudy = expectStudy;
      testDomainEntities.extend();

      // used by promise tests
      function expectStudy(entity) {
        expect(entity).toEqual(jasmine.any(self.Study));
      }
    }));

    afterEach(function() {
      this.$httpBackend.verifyNoOutstandingExpectation();
      this.$httpBackend.verifyNoOutstandingRequest();
    });

    it('constructor with no parameters has default values', function() {
      var study = new this.Study();

      expect(study.id).toBeNull();
      expect(study.version).toBe(0);
      expect(study.timeAdded).toBeNull();
      expect(study.timeModified).toBeNull();
      expect(study.name).toBeEmptyString();
      expect(study.state).toBe(this.StudyState.DISABLED);
    });

    describe('when creating', function() {

      it('can create from with empty annotation types', function() {
        var jsonStudy = _.omit(this.factory.study(), 'annotationTypes'),
            study = this.Study.create(jsonStudy);
        expect(study).toEqual(jasmine.any(this.Study));
      });

      it('fails when creating from an invalid object', function() {
        var self = this,
            badStudyJson = _.omit(self.factory.study(), 'name');

        expect(function () {
          self.Study.create(badStudyJson);
        }).toThrowError(/Missing required property/);
      });

      it('fails when creating from a non object for an annotation type', function() {
        var self = this,
            badStudyJson = self.factory.study({ annotationTypes: [ 1 ]});
        expect(function () { self.Study.create(badStudyJson); })
          .toThrowError(/bad annotation types/);
      });

    });

    it('state predicates return valid results', function() {
      var self = this;
      _.each(_.values(self.StudyState), function(state) {
        var study = new self.Study(self.factory.study({ state: state }));
        expect(study.isDisabled()).toBe(state === self.StudyState.DISABLED);
        expect(study.isEnabled()).toBe(state === self.StudyState.ENABLED);
        expect(study.isRetired()).toBe(state === self.StudyState.RETIRED);
      });
    });

    it('can retrieve a single study', function() {
      var self = this;
      self.$httpBackend.whenGET(self.uri(self.jsonStudy.id)).respond(self.reply(self.jsonStudy));
      self.Study.get(self.jsonStudy.id).then(self.expectStudy).catch(self.failTest);
      self.$httpBackend.flush();
    });

    it('fails when getting a study and it has a bad format', function() {
      var self = this,
          study = _.omit(self.jsonStudy, 'name');
      self.$httpBackend.whenGET(this.uri(study.id)).respond(this.reply(study));

      self.Study.get(study.id).then(shouldNotFail).catch(shouldFail);
      self.$httpBackend.flush();

      function shouldNotFail() {
        fail('function should not be called');
      }

      function shouldFail(error) {
        expect(error.message).toContain('Missing required property');
      }
    });

    it('fails when getting a study and it has a bad annotation type', function() {
      var self = this,
          annotationType = _.omit(self.factory.annotationType(), 'name'),
          study = self.factory.study({ annotationTypes: [ annotationType ]});

      self.$httpBackend.whenGET(this.uri(study.id)).respond(this.reply(study));

      self.Study.get(study.id).then(shouldNotFail).catch(shouldFail);
      self.$httpBackend.flush();

      function shouldNotFail(error) {
        fail('function should not be called: ' + error);
      }

      function shouldFail(error) {
        expect(error.message).toContain('bad annotation types');
      }
    });

    it('can retrieve studies', function() {
      var self = this,
          studies = [ self.factory.study({ annotationTypes: [] }) ],
          reply = self.factory.pagedResult(studies);

      self.$httpBackend.whenGET(this.uri()).respond(this.reply(reply));

      self.Study.list().then(testStudy).catch(this.failTest);
      self.$httpBackend.flush();

      function testStudy(pagedResult) {
        expect(pagedResult.items).toBeArrayOfSize(1);
        expect(pagedResult.items[0]).toEqual(jasmine.any(self.Study));
        pagedResult.items[0].compareToJsonEntity(studies[0]);
      }
    });

    it('can list studies using options', function() {
      var self = this,
          optionList = [
            { filter: 'name::test' },
            { sort: 'state' },
            { page: 2 },
            { limit: 10 }
          ],
          studies = [ self.jsonStudy ],
          reply   = self.factory.pagedResult(studies);

      _.each(optionList, function (options) {
        var url = sprintf('%s?%s', self.uri(), self.$httpParamSerializer(options, true));

        self.$httpBackend.whenGET(url).respond(self.reply(reply));

        self.Study.list(options).then(testStudy).catch(self.failTest);
        self.$httpBackend.flush();

        function testStudy(pagedResult) {
          expect(pagedResult.items).toBeArrayOfSize(studies.length);
          _.each(pagedResult.items, function (study) {
            expect(study).toEqual(jasmine.any(self.Study));
          });
        }
      });
    });

    it('when listing studies, omits empty options', function() {
      var self = this,
          options = { filter: ''},
          studies = [ self.jsonStudy ],
          reply   = self.factory.pagedResult(studies);

      self.$httpBackend.whenGET(self.uri()).respond(self.reply(reply));

      self.Study.list(options).then(testStudy).catch(self.failTest);
      self.$httpBackend.flush();

      function testStudy(pagedResult) {
        expect(pagedResult.items).toBeArrayOfSize(studies.length);
        _.each(pagedResult.items, function (study) {
          expect(study).toEqual(jasmine.any(self.Study));
        });
      }
    });

    it('fails when list returns an invalid study', function() {
      var self = this,
          studies = [ _.omit(self.jsonStudy, 'name') ],
          reply = self.factory.pagedResult(studies);

      self.$httpBackend.whenGET(this.uri()).respond(this.reply(reply));
      self.Study.list().then(listFail).catch(shouldFail);
      self.$httpBackend.flush();

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

      self.$httpBackend.expectPOST(this.uri(), json).respond(this.reply(this.jsonStudy));

      study.add().then(self.expectStudy).catch(this.failTest);
      self.$httpBackend.flush();
    });

    it('can update the name on a study', function() {
      var self  = this,
          study = new self.Study(this.jsonStudy);

      this.updateEntity.call(this,
                             study,
                             'updateName',
                             study.name,
                             this.uri('name', study.id),
                             { name: study.name },
                             this.jsonStudy,
                             self.expectStudy,
                             this.failTest);
    });

    it('can update the description on a study', function() {
      var self = this,
          study = new self.Study(this.jsonStudy);

      this.updateEntity.call(this,
                             study,
                             'updateDescription',
                             undefined,
                             this.uri('description', study.id),
                             { },
                             this.jsonStudy,
                             self.expectStudy,
                             this.failTest);

      this.updateEntity.call(this,
                             study,
                             'updateDescription',
                             study.description,
                             this.uri('description', study.id),
                             { description: study.description },
                             this.jsonStudy,
                             self.expectStudy,
                             this.failTest);
    });

    describe('for annotation types', function() {

      beforeEach(function() {
        this.annotationType = this.factory.annotationType();
        this.jsonStudy      = this.factory.study({ annotationTypes: [ this.annotationType ] });
        this.study          = this.Study.create(this.jsonStudy);
      });

      it('can add an annotation type on a study', function() {
        this.updateEntity.call(this,
                               this.study,
                               'addAnnotationType',
                               _.omit(this.annotationType, 'uniqueId'),
                               this.uri('pannottype', this.study.id),
                               _.omit(this.annotationType, 'uniqueId'),
                               this.jsonStudy,
                               this.expectStudy,
                               this.failTest);
      });

      it('can update an annotation type on a study', function() {
        this.updateEntity.call(this,
                               this.study,
                               'updateAnnotationType',
                               this.annotationType,
                               sprintf('%s/%s',
                                               this.uri('pannottype', this.study.id),
                                               this.annotationType.uniqueId),
                               this.annotationType,
                               this.jsonStudy,
                               this.expectStudy,
                               this.failTest);
      });

      it('can remove an annotation on a study', function() {
        var url = sprintf('%s/%d/%s',
                          this.uri('pannottype', this.study.id),
                          this.study.version,
                          this.annotationType.uniqueId);

        this.$httpBackend.whenDELETE(url).respond(this.reply(true));
        this.study.removeAnnotationType(this.annotationType)
          .then(this.expectStudy)
          .catch(this.failTest);
        this.$httpBackend.flush();
      });

    });

    describe('can disable a study', function() {
      var context = {};

      beforeEach(function() {
        context.jsonStudy = this.factory.study({ state: this.StudyState.ENABLED });
        context.action = 'disable';
        context.state = this.StudyState.DISABLED;
      });

      changeStatusBehaviour(context);
    });

    it('throws an error when disabling a study and it is already disabled', function() {
      var study = new this.Study(this.factory.study({ state: this.StudyState.DISABLED }));
      expect(function () { study.disable(); })
        .toThrowError('already disabled');
    });

    describe('can disable a study', function() {
      var context = {};

      beforeEach(function() {
        context.jsonStudy = this.factory.study({ state: this.StudyState.DISABLED });
        context.action = 'enable';
        context.state = this.StudyState.ENABLED;
      });

      changeStatusBehaviour(context);
    });

    it('throws an error when enabling a study and it is already enabled', function() {
      var study = new this.Study(this.factory.study({ state: this.StudyState.ENABLED }));
      expect(function () { study.enable(); })
        .toThrowError('already enabled');
    });

    describe('can disable a study', function() {
      var context = {};

      beforeEach(function() {
        context.jsonStudy = this.factory.study({ state: this.StudyState.DISABLED });
        context.action = 'retire';
        context.state = this.StudyState.RETIRED;
      });

      changeStatusBehaviour(context);
    });

    it('throws an error when retiring a study and it is already retired', function() {
      var study = new this.Study(this.factory.study({ state: this.StudyState.RETIRED }));
      expect(function () { study.retire(); })
        .toThrowError('already retired');
    });

    describe('can disable a study', function() {
      var context = {};

      beforeEach(function() {
        context.jsonStudy = this.factory.study({ state: this.StudyState.RETIRED });
        context.action = 'unretire';
        context.state = this.StudyState.DISABLED;
      });

      changeStatusBehaviour(context);
    });

    it('throws an error when unretiring a study and it is not retired', function() {
      var study = new this.Study(this.factory.study({ state: this.StudyState.DISABLED }));
      expect(function () { study.unretire(); })
        .toThrowError('not retired');

      study = new this.Study(this.factory.study({ state: this.StudyState.ENABLED }));
      expect(function () { study.unretire(); })
        .toThrowError('not retired');
    });

    it('can get a list of study names', function() {
      var self = this,
          dto = self.factory.studyNameDto();

      self.$httpBackend.whenGET('/studies/names').respond([ dto ]);
      self.Study.names()
        .then(function (reply) {
          expect(reply).toContainAll([ dto ]);
        })
        .catch(self.failTest);
      self.$httpBackend.flush();
    });

    it('can get a list of centre locations', function() {
      var self = this,
          location = self.factory.location(),
          centre = self.factory.centre({ locations: [ location ]}),
          dto = self.factory.centreLocationDto(centre),
          study = new this.Study(this.factory.study());

      self.$httpBackend.whenGET('/studies/centres/' + study.id).respond([ dto ]);
      study.allLocations()
        .then(function (reply) {
          expect(reply).toContainAll([ dto ]);
        })
        .catch(self.failTest);
      self.$httpBackend.flush();
    });

    function changeStatusBehaviour(context) {

      describe('change state shared behaviour', function() {

        it('change state', function() {
          var self  = this,
              study = new self.Study(context.jsonStudy),
              json =  { expectedVersion: study.version },
              reply = replyStudy(context.jsonStudy, { state: context.state });

          self.$httpBackend.expectPOST(self.uri(context.action, study.id), json).respond(self.reply(reply));
          expect(study[context.action]).toBeFunction();
          study[context.action]().then(checkStudy).catch(self.failTest);
          self.$httpBackend.flush();

          function checkStudy(replyStudy) {
            expect(replyStudy).toEqual(jasmine.any(self.Study));
            expect(replyStudy.state).toBe(context.state);
          }
        });

        function replyStudy(study, newValues) {
          newValues = newValues || {};
          return _.extend({}, study, newValues);
        }
      });

    }
  });

});
