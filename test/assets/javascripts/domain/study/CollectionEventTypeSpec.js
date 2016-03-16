/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'sprintf',
  'biobankApp'
], function(angular, mocks, _, sprintf) {
  'use strict';

  describe('CollectionEventType', function() {

    var CollectionEventType;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(entityTestSuite, extendedDomainEntities) {
      _.extend(this, entityTestSuite);

      this.httpBackend         = this.$injector.get('$httpBackend');
      this.CollectionEventType = this.$injector.get('CollectionEventType');
      this.jsonEntities        = this.$injector.get('jsonEntities');
      this.testUtils           = this.$injector.get('testUtils');

      this.jsonStudy = this.jsonEntities.study();
      this.jsonCet   = this.jsonEntities.collectionEventType(this.jsonStudy);

      this.testUtils.addCustomMatchers();
      CollectionEventType = this.CollectionEventType;
    }));

    afterEach(function() {
      this.httpBackend.verifyNoOutstandingExpectation();
      this.httpBackend.verifyNoOutstandingRequest();
    });

    it('constructor with no parameters has default values', function() {
      var ceventType = new this.CollectionEventType();

      expect(ceventType.isNew()).toBe(true);
      expect(ceventType.studyId).toBe(null);
      expect(ceventType.name).toBe('');
      expect(ceventType.recurring).toBe(false);
      expect(ceventType.specimenSpecs).toBeArrayOfSize(0);
      expect(ceventType.annotationTypes).toBeArrayOfSize(0);
    });

    it('fails when creating from an invalid json object', function() {
      var badJsonCet = _.omit(this.jsonEntities.collectionEventType(this.jsonStudy), 'name');
      expect(function () { CollectionEventType.create(badJsonCet); })
        .toThrowErrorOfType('Error');
    });

    it('fails when creating from a bad json specimen spec', function() {
      var jsonSpec = _.omit(this.jsonEntities.collectionSpecimenSpec(), 'name'),
          badJsonCet = _.extend(this.jsonEntities.collectionEventType(this.jsonStudy),
                                { specimenSpecs: [ jsonSpec ] });

      expect(function () { CollectionEventType.create(badJsonCet); })
        .toThrowErrorOfType('Error');
    });

    it('fails when creating from bad json annotation type data', function() {
      var jsonAnnotType = _.omit(this.jsonEntities.annotationType(), 'name'),
          badJsonCet = _.extend(this.jsonEntities.collectionEventType(this.jsonStudy),
                                { annotationTypes: [ jsonAnnotType ] });

      expect(function () { CollectionEventType.create(badJsonCet); })
        .toThrowErrorOfType('Error');
    });

    it('has valid values when creating from server response', function() {
      var jsonCet = this.jsonEntities.collectionEventType(this.jsonStudy),
          ceventType = CollectionEventType.create(jsonCet);
      ceventType.compareToJsonEntity(jsonCet);
    });

    it('can retrieve a collection event type', function() {
      var url = sprintf.sprintf('%s/%s?cetId=%s',
                                uri(),
                                this.jsonStudy.id,
                                this.jsonCet.id);

      this.httpBackend.whenGET(url).respond(serverReply(this.jsonCet));
      CollectionEventType.get(this.jsonStudy.id, this.jsonCet.id)
        .then(expectCet).catch(failTest);
      this.httpBackend.flush();
    });

    it('fails when getting a collection event type and it has a bad format', function() {
      var self = this,
          data = getBadCollectionEventTypes(self.jsonEntities, self.jsonStudy);

      _.each(data, function (item) {
        var url = sprintf.sprintf('%s/%s?cetId=%s',
                                  uri(),
                                  self.jsonStudy.id,
                                  item.cet.id);

        self.httpBackend.whenGET(url).respond(serverReply(item.cet));
        CollectionEventType.get(self.jsonStudy.id, item.cet.id)
          .then(getFail).catch(shouldFail);
        self.httpBackend.flush();

        function getFail(reply) {
          fail('function should not be called');
        }

        function shouldFail(error) {
          expect(error).toStartWith(item.errMsg);
        }
      });
    });

    it('can list collection event types', function() {
      var url = sprintf.sprintf('%s/%s', uri(), this.jsonStudy.id);

      this.httpBackend.whenGET(url).respond(serverReply([ this.jsonCet ]));
      CollectionEventType.list(this.jsonStudy.id)
        .then(expectCetArray).catch(failTest);
      this.httpBackend.flush();

      function expectCetArray(array) {
        expect(array).toBeArrayOfSize(1);
        expect(array[0]).toEqual(jasmine.any(CollectionEventType));
      }
    });

    it('fails when listing collection event types and they have a bad format', function() {
      // assigns result of self.httpBackend.whenGET() to variable so that the response
      // can be changed inside the loop
      var self = this,
          data = getBadCollectionEventTypes(self.jsonEntities, self.jsonStudy),
          url = sprintf.sprintf('%s/%s', uri(), self.jsonStudy.id),
          reqHandler = self.httpBackend.whenGET(url);

      _.each(data, function (item) {
        reqHandler.respond(serverReply([ item.cet ]));
        CollectionEventType.list(self.jsonStudy.id).then(getFail).catch(shouldFail);
        self.httpBackend.flush();

        function getFail(reply) {
          fail('function should not be called');
        }

        function shouldFail(error) {
          expect(error.message).toStartWith(item.errMsg);
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
      var ceventType = new CollectionEventType(this.jsonCet),
          url = sprintf.sprintf('%s/%s', uri(), this.jsonStudy.id);

      this.httpBackend.expectPOST(url).respond(201, serverReply(this.jsonCet));

      ceventType.add().then(expectCet).catch(failTest);
      this.httpBackend.flush();
    });

    it('should remove a collection event type', function() {
      var ceventType = new CollectionEventType(this.jsonCet),
          url = sprintf.sprintf('%s/%s/%s/%d',
                                uri(),
                                this.jsonStudy.id,
                                ceventType.id,
                                ceventType.version);

      this.httpBackend.expectDELETE(url).respond(201, serverReply(true));
      ceventType.remove();
      this.httpBackend.flush();
    });

    it('should update name', function () {
      var cet = new this.CollectionEventType(this.jsonCet);
      this.updateEntity.call(this,
                             cet,
                             'updateName',
                             cet.name,
                             uri('name', cet.id),
                             { name: cet.name, studyId: cet.studyId },
                             this.jsonCet,
                             expectCet,
                             failTest);
    });

    it('should update description', function () {
      var cet = new this.CollectionEventType(this.jsonCet);

      this.updateEntity.call(this,
                             cet,
                             'updateDescription',
                             undefined,
                             uri('description', cet.id),
                             { studyId: cet.studyId },
                             this.jsonCet,
                             expectCet,
                             failTest);

      this.updateEntity.call(this,
                             cet,
                             'updateDescription',
                             cet.description,
                             uri('description', cet.id),
                             { description: cet.description, studyId: cet.studyId },
                             this.jsonCet,
                             expectCet,
                             failTest);
    });

    it('should update recurring', function () {
      var cet = new this.CollectionEventType(this.jsonCet);
      this.updateEntity.call(this,
                             cet,
                             'updateRecurring',
                             cet.recurring,
                             uri('recurring', cet.id),
                             { recurring: cet.recurring, studyId: cet.studyId },
                             this.jsonCet,
                             expectCet,
                             failTest);
    });

    it('should add a specimen spec', function () {
      var jsonSpec = this.jsonEntities.collectionSpecimenSpec(),
          cet = new this.CollectionEventType(this.jsonCet);
      this.updateEntity.call(this,
                             cet,
                             'addSpecimenSpec',
                             _.omit(jsonSpec, 'uniqueId'),
                             uri('spcspec', cet.id),
                             _.extend(_.omit(jsonSpec, 'uniqueId'), { studyId: cet.studyId }),
                             this.jsonCet,
                             expectCet,
                             failTest);
    });

    it('should remove a specimen spec', function () {
      var jsonSpec = this.jsonEntities.collectionSpecimenSpec(),
          jsonCet  = _.extend(this.jsonCet, { specimenSpecs: [ jsonSpec ]}),
          cet      = new this.CollectionEventType(jsonCet),
          url      = sprintf.sprintf('%s/%d/%s', uri('spcspec', cet.id),
                                     cet.version, jsonSpec.uniqueId);

      this.httpBackend.whenDELETE(url).respond(201, serverReply(true));
      cet.removeSpecimenSpec(jsonSpec).then(expectCet).catch(failTest);
      this.httpBackend.flush();
    });

    it('should add an annotation type', function () {
      var jsonAnnotType = this.jsonEntities.collectionSpecimenSpec(),
          cet = new this.CollectionEventType(this.jsonCet);
      this.updateEntity.call(this,
                             cet,
                             'addAnnotationType',
                             _.omit(jsonAnnotType, 'uniqueId'),
                             uri('annottype', cet.id),
                             _.extend(_.omit(jsonAnnotType, 'uniqueId'), { studyId: cet.studyId }),
                             this.jsonCet,
                             expectCet,
                             failTest);
    });

    it('should remove an annotation type', function () {
      var jsonAnnotType = this.jsonEntities.collectionSpecimenSpec(),
          jsonCet       = _.extend(this.jsonCet, { annotationTypes: [ jsonAnnotType ]}),
          cet           = new this.CollectionEventType(jsonCet),
          url           = sprintf.sprintf('%s/%s/%d/%s',
                                          uri('annottype', cet.studyId),
                                          cet.id,
                                          cet.version,
                                          jsonAnnotType.uniqueId);

      this.httpBackend.whenDELETE(url).respond(201, serverReply(true));
      cet.removeAnnotationType(jsonAnnotType).then(expectCet).catch(failTest);
      this.httpBackend.flush();
    });

    // used by promise tests
    function expectCet(entity) {
      expect(entity).toEqual(jasmine.any(CollectionEventType));
    }

    // used by promise tests
    function failTest(error) {
      expect(error).toBeUndefined();
    }

    function serverReply(obj) {
      return { status: 'success', data: obj };
    }

    /**
     * Returns 3 collection event types, each one with a different missing field.
     */
    function getBadCollectionEventTypes(jsonEntities, jsonStudy) {
      var badSpecimenSpec   = _.omit(jsonEntities.collectionSpecimenSpec(), 'name'),
          badAnnotationType = _.omit(jsonEntities.annotationType(), 'name'),
          data = [
            {
              cet: _.omit(jsonEntities.collectionEventType(jsonStudy), 'name'),
              errMsg : 'invalid collection event types from server'
            },
            {
              cet: jsonEntities.collectionEventType(jsonStudy,
                                                    { specimenSpecs: [ badSpecimenSpec ]}),
              errMsg : 'invalid specimen specs from server'
            },
            {
              cet: jsonEntities.collectionEventType(jsonStudy,
                                                    { annotationTypes: [ badAnnotationType ]}),
              errMsg : 'invalid annotation types from server'
            }
          ];
      return data;
    }

    function uri(/* path, cetypeId */) {
      var args = _.toArray(arguments),
          cetypeId,
          path;

      var result = '/studies/cetypes';

      if (args.length > 0) {
        path = args.shift();
        result += '/' + path;
      }

      if (args.length > 0) {
        cetypeId = args.shift();
        result += '/' + cetypeId;
      }

      return result;
    }
  });

});
