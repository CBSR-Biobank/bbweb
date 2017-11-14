/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Study', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(EntityTestSuiteMixin,
                                 ServerReplyMixin) {
      _.extend(this, EntityTestSuiteMixin, ServerReplyMixin);
      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'Study',
                              'StudyState',
                              'Factory',
                              'TestUtils');

      this.TestUtils.addCustomMatchers();
      this.jsonStudy = this.Factory.study();

      // used by promise tests
      this.expectStudy = (entity) => {
        expect(entity).toEqual(jasmine.any(this.Study));
      };

      this.url = url;

      //---

      function url() {
        const args = [ 'studies' ].concat(_.toArray(arguments));
        return EntityTestSuiteMixin.url.apply(null, args);
      }
    });
  });

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
      var jsonStudy = _.omit(this.Factory.study(), 'annotationTypes'),
          study = this.Study.create(jsonStudy);
      expect(study).toEqual(jasmine.any(this.Study));
    });

    it('fails when creating from an invalid object', function() {
      var badStudyJson = _.omit(this.Factory.study(), 'name');

      expect(() => {
        this.Study.create(badStudyJson);
      }).toThrowError(/Missing required property/);
    });

    it('fails when creating from a non object for an annotation type', function() {
      var badStudyJson = this.Factory.study({ annotationTypes: [ 1 ]});
      expect(() => { this.Study.create(badStudyJson); })
        .toThrowError(/Invalid type/);
    });

  });

  it('state predicates return valid results', function() {
    _.values(this.StudyState).forEach((state) => {
      var study = new this.Study(this.Factory.study({ state: state }));
      expect(study.isDisabled()).toBe(state === this.StudyState.DISABLED);
      expect(study.isEnabled()).toBe(state === this.StudyState.ENABLED);
      expect(study.isRetired()).toBe(state === this.StudyState.RETIRED);
    });
  });

  describe('when getting a single study', function() {

    it('can retrieve a single study', function() {
      this.$httpBackend.whenGET(this.url(this.jsonStudy.id)).respond(this.reply(this.jsonStudy));
      this.Study.get(this.jsonStudy.id).then(this.expectStudy.bind(this)).catch(failTest);
      this.$httpBackend.flush();
    });

    it('fails when getting a study and it has a bad format', function() {
      var study = _.omit(this.jsonStudy, 'name');
      this.$httpBackend.whenGET(this.url(study.id)).respond(this.reply(study));
      this.Study.get(study.id).then(shouldNotFail).catch(shouldFail);
      this.$httpBackend.flush();
    });

    it('fails when getting a study and it has a bad annotation type', function() {
      var annotationType = _.omit(this.Factory.annotationType(), 'name'),
          study = this.Factory.study({ annotationTypes: [ annotationType ]});

      this.$httpBackend.whenGET(this.url(study.id)).respond(this.reply(study));
      this.Study.get(study.id).then(shouldNotFail).catch(shouldFail);
      this.$httpBackend.flush();
    });

  });

  describe('when searching studies', function() {

    it('can retrieve studies', function() {
      var studies = [ this.Factory.study({ annotationTypes: [] }) ],
          reply = this.Factory.pagedResult(studies),
          testStudy = (pagedResult) => {
            expect(pagedResult.items).toBeArrayOfSize(1);
            expect(pagedResult.items[0]).toEqual(jasmine.any(this.Study));
          };

      this.$httpBackend.whenGET(this.url('search')).respond(this.reply(reply));
      this.Study.list().then(testStudy).catch(failTest);
      this.$httpBackend.flush();
    });

    it('can use options', function() {
      var optionList = [
            { filter: 'name::test' },
            { sort: 'state' },
            { page: 2 },
            { limit: 10 }
          ],
          studies = [ this.jsonStudy ],
          reply   = this.Factory.pagedResult(studies);

      optionList.forEach((options) => {
        var url = this.url('search') + '?' + this.$httpParamSerializer(options, true),
            testStudy = (pagedResult) => {
              expect(pagedResult.items).toBeArrayOfSize(studies.length);
              pagedResult.items.forEach((study) => {
                expect(study).toEqual(jasmine.any(this.Study));
              });
            };

        this.$httpBackend.whenGET(url).respond(this.reply(reply));
        this.Study.list(options).then(testStudy).catch(failTest);
        this.$httpBackend.flush();
      });
    });

    it('listing omits empty options', function() {
      var options = { filter: ''},
          studies = [ this.jsonStudy ],
          reply   = this.Factory.pagedResult(studies),
          testStudy = (pagedResult) => {
            expect(pagedResult.items).toBeArrayOfSize(studies.length);
            pagedResult.items.forEach((study) => {
              expect(study).toEqual(jasmine.any(this.Study));
            });
          };

      this.$httpBackend.whenGET(this.url('search')).respond(this.reply(reply));
      this.Study.list(options).then(testStudy).catch(failTest);
      this.$httpBackend.flush();
    });

    it('fails when an invalid study is returned', function() {
      var studies = [ _.omit(this.jsonStudy, 'name') ],
          reply = this.Factory.pagedResult(studies);

      this.$httpBackend.whenGET(this.url('search')).respond(this.reply(reply));
      this.Study.list().then(listFail).catch(shouldFail);
      this.$httpBackend.flush();

      function listFail() {
        fail('function should not be called');
      }

      function shouldFail(error) {
        expect(error).toStartWith('invalid studies from server');
      }
    });

  });

  it('can add a study', function() {
    var study = new this.Study(_.omit(this.jsonStudy, 'id')),
        json = _.pick(study, 'name', 'description');

    this.$httpBackend.expectPOST(this.url(''), json).respond(this.reply(this.jsonStudy));
    study.add().then(this.expectStudy.bind(this)).catch(failTest);
    this.$httpBackend.flush();
  });

  it('can update the name on a study', function() {
    var study = new this.Study(this.jsonStudy);

    this.updateEntity.call(this,
                           study,
                           'updateName',
                           study.name,
                           this.url('name', study.id),
                           { name: study.name },
                           this.jsonStudy,
                           this.expectStudy.bind(this),
                           failTest);
  });

  it('can update the description on a study', function() {
    var study = new this.Study(this.jsonStudy);

    this.updateEntity.call(this,
                           study,
                           'updateDescription',
                           undefined,
                           this.url('description', study.id),
                           { },
                           this.jsonStudy,
                           this.expectStudy.bind(this),
                           failTest);

    this.updateEntity.call(this,
                           study,
                           'updateDescription',
                           study.description,
                           this.url('description', study.id),
                           { description: study.description },
                           this.jsonStudy,
                           this.expectStudy.bind(this),
                           failTest);
  });

  describe('for annotation types', function() {

    beforeEach(function() {
      this.annotationType = this.Factory.annotationType();
      this.jsonStudy      = this.Factory.study({ annotationTypes: [ this.annotationType ] });
      this.study          = this.Study.create(this.jsonStudy);
    });

    it('can add an annotation type on a study', function() {
      this.updateEntity.call(this,
                             this.study,
                             'addAnnotationType',
                             _.omit(this.annotationType, 'id'),
                             this.url('pannottype', this.study.id),
                             _.omit(this.annotationType, 'id'),
                             this.jsonStudy,
                             this.expectStudy.bind(this),
                             failTest);
    });

    it('can update an annotation type on a study', function() {
      this.updateEntity.call(this,
                             this.study,
                             'updateAnnotationType',
                             this.annotationType,
                             this.url('pannottype', this.study.id, this.annotationType.id),
                             this.annotationType,
                             this.jsonStudy,
                             this.expectStudy.bind(this),
                             failTest);
    });

    it('can remove an annotation on a study', function() {
      var url = this.url('pannottype', this.study.id, this.study.version, this.annotationType.id);

      this.$httpBackend.whenDELETE(url).respond(this.reply(this.jsonStudy));
      this.study.removeAnnotationType(this.annotationType)
        .then(this.expectStudy.bind(this))
        .catch(failTest);
      this.$httpBackend.flush();
    });

  });

  describe('can disable a study', function() {
    var context = {};

    beforeEach(function() {
      context.jsonStudy = this.Factory.study({ state: this.StudyState.ENABLED });
      context.action = 'disable';
      context.state = this.StudyState.DISABLED;
    });

    changeStatusBehaviour(context);
  });

  it('throws an error when disabling a study and it is already disabled', function() {
    var study = new this.Study(this.Factory.study({ state: this.StudyState.DISABLED }));
    expect(() => { study.disable(); })
      .toThrowError('already disabled');
  });

  describe('can disable a study', function() {
    var context = {};

    beforeEach(function() {
      context.jsonStudy = this.Factory.study({ state: this.StudyState.DISABLED });
      context.action = 'enable';
      context.state = this.StudyState.ENABLED;
    });

    changeStatusBehaviour(context);
  });

  it('throws an error when enabling a study and it is already enabled', function() {
    var study = new this.Study(this.Factory.study({ state: this.StudyState.ENABLED }));
    expect(() => { study.enable(); })
      .toThrowError('already enabled');
  });

  describe('can disable a study', function() {
    var context = {};

    beforeEach(function() {
      context.jsonStudy = this.Factory.study({ state: this.StudyState.DISABLED });
      context.action = 'retire';
      context.state = this.StudyState.RETIRED;
    });

    changeStatusBehaviour(context);
  });

  it('throws an error when retiring a study and it is already retired', function() {
    var study = new this.Study(this.Factory.study({ state: this.StudyState.RETIRED }));
    expect(() => { study.retire(); })
      .toThrowError('already retired');
  });

  describe('can disable a study', function() {
    var context = {};

    beforeEach(function() {
      context.jsonStudy = this.Factory.study({ state: this.StudyState.RETIRED });
      context.action = 'unretire';
      context.state = this.StudyState.DISABLED;
    });

    changeStatusBehaviour(context);
  });

  it('throws an error when unretiring a study and it is not retired', function() {
    var study = new this.Study(this.Factory.study({ state: this.StudyState.DISABLED }));
    expect(() => { study.unretire(); })
      .toThrowError('not retired');

    study = new this.Study(this.Factory.study({ state: this.StudyState.ENABLED }));
    expect(() => { study.unretire(); })
      .toThrowError('not retired');
  });

  it('can get a list of centre locations', function() {
    var location = this.Factory.location(),
        centre = this.Factory.centre({ locations: [ location ]}),
        dto = this.Factory.centreLocationDto(centre),
        study = new this.Study(this.Factory.study());

    this.$httpBackend.whenGET(this.url('centres',  study.id)).respond([ dto ]);
    study.allLocations()
      .then((reply) => {
        expect(reply).toContainAll([ dto ]);
      })
      .catch(failTest);
    this.$httpBackend.flush();
  });

  it('can query if a study can be enabled', function() {
    var study = new this.Study(this.jsonStudy);
    this.$httpBackend.whenGET(this.url('enableAllowed', study.id)).respond(this.reply(false));
    study.isEnableAllowed()
      .then((reply) => {
        expect(reply).toBeFalse();
      })
      .catch(failTest);
    this.$httpBackend.flush();
  });


  function changeStatusBehaviour(context) {

    describe('change state shared behaviour', function() {

      it('change state', function() {
        var study = new this.Study(context.jsonStudy),
            json =  { expectedVersion: study.version },
            reply = replyStudy(context.jsonStudy, { state: context.state }),
            checkStudy = (replyStudy) => {
              expect(replyStudy).toEqual(jasmine.any(this.Study));
              expect(replyStudy.state).toBe(context.state);
            };

        this.$httpBackend.expectPOST(this.url(context.action, study.id), json).respond(this.reply(reply));
        expect(study[context.action]).toBeFunction();
        study[context.action]().then(checkStudy).catch(failTest);
        this.$httpBackend.flush();
      });

      function replyStudy(study, newValues) {
        newValues = newValues || {};
        return _.extend({}, study, newValues);
      }
    });

  }

  function shouldNotFail() {
    fail('function should not be called');
  }

  function shouldFail(error) {
    expect(error.message).toContain('Missing required property');
  }

  // used by promise tests
  function failTest(error) {
    expect(error).toBeUndefined();
  }

});
