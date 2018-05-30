/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { EntityTestSuiteMixin } from 'test/mixins/EntityTestSuiteMixin';
import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import _ from 'lodash';
import ngModule from '../../index'

describe('CollectionEventType', function() {

  var CollectionEventType;

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, EntityTestSuiteMixin, ServerReplyMixin);

      this.injectDependencies('$rootScope',
                              '$httpBackend',
                              'Study',
                              'CollectionEventType',
                              'Factory');

      this.jsonCet   = this.Factory.collectionEventType();
      this.jsonStudy = this.Factory.defaultStudy();

      this.addCustomMatchers();
      CollectionEventType = this.CollectionEventType;

      // used by promise tests
      this.expectCet = (entity) => {
        expect(entity).toEqual(jasmine.any(CollectionEventType));
      };

      // used by promise tests
      this.failTest = (error) => {
        expect(error).toBeUndefined();
      };

      /*
       * Returns 3 collection event types, each one with a different missing field.
       */
      this.getBadCollectionEventTypes = () => {
        const badSpecimenDefinition   = _.omit(this.Factory.collectionSpecimenDefinition(), 'name'),
              badAnnotationType = _.omit(this.Factory.annotationType(), 'name');

        return [
          {
            cet: _.omit(this.Factory.collectionEventType(), 'name'),
            errMsg : 'Missing required property'
          },
          {
            cet: this.Factory.collectionEventType({ specimenDefinitions: [ badSpecimenDefinition ]}),
            errMsg : 'specimenDefinitions.*Missing required property'
          },
          {
            cet: this.Factory.collectionEventType({ annotationTypes: [ badAnnotationType ]}),
            errMsg : 'annotationTypes.*Missing required property'
          }
        ];
      };

      this.url = (...paths) => {
        const args = [ 'studies/cetypes' ].concat(paths);
        return EntityTestSuiteMixin.url.apply(null, args);
      }
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  it('constructor with no parameters has default values', function() {
    var ceventType = new this.CollectionEventType();

    expect(ceventType.isNew()).toBe(true);
    expect(ceventType.studyId).toBeUndefined();
    expect(ceventType.name).toBeUndefined();
    expect(ceventType.recurring).toBe(false);
    expect(ceventType.specimenDefinitions).toBeArrayOfSize(0);
    expect(ceventType.annotationTypes).toBeArrayOfSize(0);
  });

  it('fails when creating from an invalid json object', function() {
    var badJsonCet = _.omit(this.Factory.collectionEventType(this.jsonStudy), 'name');
    expect(() => {
      CollectionEventType.create(badJsonCet);
    }).toThrowError(/invalid collection event type from server/);
  });

  it('fails when creating from a bad json specimen definition', function() {
    var jsonSpec = _.omit(this.Factory.collectionSpecimenDefinition(), 'name'),
        badJsonCet = Object.assign(this.Factory.collectionEventType(this.jsonStudy),
                                   { specimenDefinitions: [ jsonSpec ] });

    expect(() => CollectionEventType.create(badJsonCet))
      .toThrowError(/specimenDefinitions.*Missing required property/);
  });

  it('fails when creating from bad json annotation type data', function() {
    var jsonAnnotType = _.omit(this.Factory.annotationType(), 'name'),
        badJsonCet = Object.assign(this.Factory.collectionEventType(this.jsonStudy),
                                   { annotationTypes: [ jsonAnnotType ] });

    expect(() => CollectionEventType.create(badJsonCet))
      .toThrowError(/annotationTypes.*Missing required property/);
  });

  it('can retrieve a collection event type', function() {
    var url = this.url(this.jsonStudy.slug, this.jsonCet.slug);

    this.$httpBackend.whenGET(url).respond(this.reply(this.jsonCet));
    CollectionEventType.get(this.jsonStudy.slug, this.jsonCet.slug)
      .then(this.expectCet).catch(this.failTest);
    this.$httpBackend.flush();
  });

  it('fails when getting a collection event type and it has a bad format', function() {
    this.getBadCollectionEventTypes().forEach((badCet) => {
      var url = this.url(this.jsonStudy.slug, badCet.cet.slug);

      this.$httpBackend.whenGET(url).respond(this.reply(badCet.cet));
      CollectionEventType.get(this.jsonStudy.slug, badCet.cet.slug)
        .then(getFail).catch(shouldFail);
      this.$httpBackend.flush();

      function shouldFail(error) {
        expect(error.message).toMatch(badCet.errMsg);
      }
    });

    function getFail() {
      fail('function should not be called');
    }
  });

  it('can list collection event types', function() {
    var url = this.url(this.jsonStudy.slug),
        reply = this.Factory.pagedResult([ this.jsonCet ]);

    this.$httpBackend.whenGET(url).respond(this.reply(reply));
    CollectionEventType.list(this.jsonStudy.slug)
      .then(expectPagedResult)
      .catch(this.failTest);
    this.$httpBackend.flush();

    function expectPagedResult(pagedResult) {
      expect(pagedResult.items).toBeArrayOfSize(1);
      expect(pagedResult.items[0]).toEqual(jasmine.any(CollectionEventType));
    }
  });

  it('fails when listing collection event types and they have a bad format', function() {
    var data = this.getBadCollectionEventTypes(),
        url = this.url(this.jsonStudy.slug);

    data.forEach((item) => {
      this.$httpBackend.expectGET(url).respond(this.reply(this.Factory.pagedResult([ item.cet ])));
      CollectionEventType.list(this.jsonStudy.slug).then(getFail).catch(shouldFail);
      this.$httpBackend.flush();

      function getFail() {
        fail('function should not be called');
      }

      function shouldFail(error) {
        expect(error).toMatch(item.errMsg);
      }
    });
  });

  it('isNew should be true for a collection event type with no ID', function() {
    var cet = new CollectionEventType(_.omit(this.jsonCet, 'id'));
    expect(cet.isNew()).toBe(true);
  });

  it('isNew should be false for a collection event type that has an ID', function() {
    var cet = new CollectionEventType(this.jsonCet);
    expect(cet.isNew()).toBe(false);
  });

  it('can add a collection event type', function() {
    var study = this.Study.create(this.jsonStudy),
        ceventType = new CollectionEventType(this.jsonCet, { study: study }),
        url = this.url(this.jsonStudy.id);

    this.$httpBackend.expectPOST(url).respond(this.reply(this.jsonCet));

    ceventType.add().then(this.expectCet).catch(this.failTest);
    this.$httpBackend.flush();
  });

  it('should remove a collection event type', function() {
    var study = this.Study.create(this.jsonStudy),
        ceventType = new CollectionEventType(this.jsonCet, { study: study }),
        url = this.url(this.jsonStudy.id, ceventType.id, ceventType.version);

    this.$httpBackend.expectDELETE(url).respond(this.reply(true));
    ceventType.remove()
      .then((result) => {
        expect(result).toBeTrue();
      })
      .catch(this.failTest);
    this.$httpBackend.flush();
  });

  it('should update name', function () {
    var cet = this.CollectionEventType.create(this.jsonCet);
    this.updateEntity(cet,
                      'updateName',
                      cet.name,
                      this.url('name', cet.id),
                      { name: cet.name, studyId: cet.studyId },
                      this.jsonCet,
                      this.expectCet.bind(this),
                      this.failTest.bind(this));
  });

  it('should update description', function () {
    var cet = this.CollectionEventType.create(this.jsonCet);
    this.updateEntity(cet,
                      'updateDescription',
                      undefined,
                      this.url('description', cet.id),
                      { studyId: cet.studyId },
                      this.jsonCet,
                      this.expectCet.bind(this),
                      this.failTest.bind(this));

    this.updateEntity(cet,
                      'updateDescription',
                      cet.description,
                      this.url('description', cet.id),
                      { description: cet.description, studyId: cet.studyId },
                      this.jsonCet,
                      this.expectCet.bind(this),
                      this.failTest.bind(this));
  });

  it('should update recurring', function () {
    var cet = new this.CollectionEventType(this.jsonCet);
    this.updateEntity(cet,
                      'updateRecurring',
                      cet.recurring,
                      this.url('recurring', cet.id),
                      { recurring: cet.recurring, studyId: cet.studyId },
                      this.jsonCet,
                      this.expectCet.bind(this),
                      this.failTest.bind(this));
  });

  describe('for specimen definitions', function() {

    beforeEach(function() {
      this.jsonSpec = this.Factory.collectionSpecimenDefinition();
      this.jsonCet  = this.Factory.collectionEventType({ specimenDefinitions: [ this.jsonSpec ] });
      this.cet      = this.CollectionEventType.create(this.jsonCet);
    });

    it('should add a specimen description', function () {
      this.updateEntity(this.cet,
                        'addSpecimenDefinition',
                        _.omit(this.jsonSpec, 'id'),
                        this.url('spcdesc', this.cet.id),
                        Object.assign(_.omit(this.jsonSpec, 'id'), { studyId: this.cet.studyId }),
                        this.jsonCet,
                        this.expectCet.bind(this),
                        this.failTest.bind(this));
    });

    it('should remove a specimen description', function () {
      var url = this.url('spcdesc', this.cet.studyId, this.cet.id, this.cet.version, this.jsonSpec.id);
      this.$httpBackend.whenDELETE(url).respond(this.reply(this.jsonCet));
      this.cet.removeSpecimenDefinition(this.jsonSpec).then(this.expectCet).catch(this.failTest);
      this.$httpBackend.flush();
    });

    it('throws an error when attempting to remove an invalid specimen definition', function () {
      this.cet.specimenDefinitions = [];
      expect(() => {
        this.cet.removeSpecimenDefinition(this.jsonSpec).then(this.expectCet).catch(this.failTest);
      }).toThrowError(/specimen description with ID not present/);
    });

  });

  describe('for annotation types', function() {

    beforeEach(function() {
      var study = this.Study.create(this.jsonStudy);

      this.jsonAnnotType = this.Factory.annotationType();
      this.jsonCet       = this.Factory.collectionEventType({ annotationTypes: [ this.jsonAnnotType ]});
      this.cet           = this.CollectionEventType.create(this.jsonCet, { study: study });
    });

    it('should add an annotation type', function () {
      this.updateEntity.call(this,
                             this.cet,
                             'addAnnotationType',
                             _.omit(this.jsonAnnotType, 'id'),
                             this.url('annottype', this.cet.id),
                             Object.assign(_.omit(this.jsonAnnotType, 'id'),
                                           { studyId: this.cet.studyId }),
                             this.jsonCet,
                             this.expectCet,
                             this.failTest);
    });

    describe('removing an annotation type', function() {

      it('should remove an annotation type', function () {
        var url = this.url('annottype', this.cet.studyId, this.cet.id, this.cet.version, this.jsonAnnotType.id),
            cetCheck = (ceventType) => {
              expect(ceventType).toEqual(jasmine.any(this.CollectionEventType));
            };

        this.$httpBackend.whenDELETE(url).respond(this.reply(this.jsonCet));
        this.cet.removeAnnotationType(this.jsonAnnotType)
          .then(cetCheck)
          .catch(this.failTest);
        this.$httpBackend.flush();
      });

      it('fails when removing an invalid annotation type', function() {
        var jsonAnnotType = Object.assign({}, this.jsonAnnotType, { id: this.Factory.stringNext() });
        this.cet.removeAnnotationType(jsonAnnotType)
          .catch((err) => {
            expect(err.message).toContain('annotation type with ID not present:');
          });
        this.$rootScope.$digest();
      });

    });

  });

  it('inUse has valid URL and returns FALSE', function() {
    var cet     = new this.CollectionEventType(this.Factory.collectionEventType());
    this.$httpBackend.whenGET(this.url('inuse', cet.id)).respond(this.reply(false));
    cet.inUse()
      .then((reply) => {
        expect(reply).toBe(false);
      })
      .catch(this.failTest);
    this.$httpBackend.flush();
  });

  it('inUse has valid URL and returns TRUE', function() {
    var cet     = new this.CollectionEventType(this.Factory.collectionEventType());
    this.$httpBackend.whenGET(this.url('inuse', cet.id)).respond(this.reply(true));
    cet.inUse()
      .then((reply) => {
        expect(reply).toBe(true);
      })
      .catch(this.failTest);
    this.$httpBackend.flush();
  });

});
