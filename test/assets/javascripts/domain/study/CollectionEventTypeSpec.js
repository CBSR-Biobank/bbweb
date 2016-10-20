/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks   = require('angularMocks'),
      _       = require('lodash'),
      sprintf = require('sprintf').sprintf;

  var CollectionEventType;

  function SuiteMixinFactory(EntityTestSuiteMixin, ServerReplyMixin) {

    function SuiteMixin() {
      EntityTestSuiteMixin.call(this);
      ServerReplyMixin.call(this);
    }

    SuiteMixin.prototype = Object.create(EntityTestSuiteMixin.prototype);
    _.extend(SuiteMixin.prototype, ServerReplyMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    // used by promise tests
    SuiteMixin.prototype.expectCet = function (entity) {
      expect(entity).toEqual(jasmine.any(CollectionEventType));
    };

    // used by promise tests
    SuiteMixin.prototype.failTest = function (error) {
      expect(error).toBeUndefined();
    };

    /**
     * Returns 3 collection event types, each one with a different missing field.
     */
    SuiteMixin.prototype.getBadCollectionEventTypes = function (jsonStudy) {
      var badSpecimenSpec   = _.omit(this.factory.collectionSpecimenSpec(), 'name'),
          badAnnotationType = _.omit(this.factory.annotationType(), 'name'),
          data = [
            {
              cet: _.omit(this.factory.collectionEventType(), 'name'),
              errMsg : 'invalid collection event types from server'
            },
            {
              cet: this.factory.collectionEventType({ specimenSpecs: [ badSpecimenSpec ]}),
              errMsg : 'invalid specimen specs from server'
            },
            {
              cet: this.factory.collectionEventType({ annotationTypes: [ badAnnotationType ]}),
              errMsg : 'invalid annotation types from server'
            }
          ];
      return data;
    };

    SuiteMixin.prototype.uri = function (/* path, cetypeId */) {
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
    };

    return SuiteMixin;
  }

  describe('CollectionEventType', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(EntityTestSuiteMixin, ServerReplyMixin, extendedDomainEntities) {
      var SuiteMixin = new SuiteMixinFactory(EntityTestSuiteMixin, ServerReplyMixin);
      _.extend(this, SuiteMixin.prototype);

      this.injectDependencies('$rootScope',
                              '$httpBackend',
                              'CollectionEventType',
                              'factory',
                              'testUtils');

      this.jsonCet   = this.factory.collectionEventType();
      this.jsonStudy = this.factory.defaultStudy();

      this.testUtils.addCustomMatchers();
      CollectionEventType = this.CollectionEventType;
    }));

    afterEach(function() {
      this.$httpBackend.verifyNoOutstandingExpectation();
      this.$httpBackend.verifyNoOutstandingRequest();
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
      var badJsonCet = _.omit(this.factory.collectionEventType(this.jsonStudy), 'name');
      expect(function () { CollectionEventType.create(badJsonCet); })
        .toThrowError(/invalid collection event types from server/);
    });

    it('fails when creating from a bad json specimen spec', function() {
      var jsonSpec = _.omit(this.factory.collectionSpecimenSpec(), 'name'),
          badJsonCet = _.extend(this.factory.collectionEventType(this.jsonStudy),
                                { specimenSpecs: [ jsonSpec ] });

      expect(function () { CollectionEventType.create(badJsonCet); })
        .toThrowError(/invalid specimen specs from server/);
    });

    it('fails when creating from bad json annotation type data', function() {
      var jsonAnnotType = _.omit(this.factory.annotationType(), 'name'),
          badJsonCet = _.extend(this.factory.collectionEventType(this.jsonStudy),
                                { annotationTypes: [ jsonAnnotType ] });

      expect(function () { CollectionEventType.create(badJsonCet); })
        .toThrowError(/invalid annotation types from server/);
    });

    it('has valid values when creating from server response', function() {
      var jsonCet = this.factory.collectionEventType(this.jsonStudy),
          ceventType = CollectionEventType.create(jsonCet);
      ceventType.compareToJsonEntity(jsonCet);
    });

   it('can retrieve a collection event type', function() {
      var url = sprintf('%s/%s?cetId=%s',
                        this.uri(),
                        this.jsonStudy.id,
                        this.jsonCet.id);

      this.$httpBackend.whenGET(url).respond(this.reply(this.jsonCet));
      CollectionEventType.get(this.jsonStudy.id, this.jsonCet.id)
        .then(this.expectCet).catch(this.failTest);
      this.$httpBackend.flush();
    });

    it('fails when getting a collection event type and it has a bad format', function() {
      var self = this,
          data = self.getBadCollectionEventTypes(self.jsonStudy);

      _.each(data, function (badCet) {
        var url = sprintf('%s/%s?cetId=%s',
                          self.uri(),
                          self.jsonStudy.id,
                          badCet.cet.id);

        self.$httpBackend.whenGET(url).respond(self.reply(badCet.cet));
        CollectionEventType.get(self.jsonStudy.id, badCet.cet.id)
          .then(getFail).catch(shouldFail);
        self.$httpBackend.flush();

        function shouldFail(error) {
          expect(error).toStartWith(badCet.errMsg);
        }
      });

      function getFail(reply) {
        fail('function should not be called');
      }
    });

    it('can list collection event types', function() {
      var url = sprintf('%s/%s', this.uri(), this.jsonStudy.id);

      this.$httpBackend.whenGET(url).respond(this.reply([ this.jsonCet ]));
      CollectionEventType.list(this.jsonStudy.id)
        .then(expectCetArray).catch(this.failTest);
      this.$httpBackend.flush();

      function expectCetArray(array) {
        expect(array).toBeArrayOfSize(1);
        expect(array[0]).toEqual(jasmine.any(CollectionEventType));
      }
    });

    it('fails when listing collection event types and they have a bad format', function() {
      // assigns result of self.$httpBackend.whenGET() to variable so that the response
      // can be changed inside the loop
      var self = this,
          data = self.getBadCollectionEventTypes(self.jsonStudy),
          url = sprintf('%s/%s', self.uri(), self.jsonStudy.id),
          reqHandler = self.$httpBackend.whenGET(url);

      _.each(data, function (item) {
        reqHandler.respond(self.reply([ item.cet ]));
        CollectionEventType.list(self.jsonStudy.id).then(getFail).catch(shouldFail);
        self.$httpBackend.flush();

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
          url = sprintf('%s/%s', this.uri(), this.jsonStudy.id);

      this.$httpBackend.expectPOST(url).respond(this.reply(this.jsonCet));

      ceventType.add().then(this.expectCet).catch(this.failTest);
      this.$httpBackend.flush();
    });

    it('should remove a collection event type', function() {
      var ceventType = new CollectionEventType(this.jsonCet),
          url = sprintf('%s/%s/%s/%d',
                                this.uri(),
                                this.jsonStudy.id,
                                ceventType.id,
                                ceventType.version);

      this.$httpBackend.expectDELETE(url).respond(this.reply(true));
      ceventType.remove();
      this.$httpBackend.flush();
    });

    it('should update name', function () {
      var cet = new this.CollectionEventType(this.jsonCet);
      this.updateEntity.call(this,
                             cet,
                             'updateName',
                             cet.name,
                             this.uri('name', cet.id),
                             { name: cet.name, studyId: cet.studyId },
                             this.jsonCet,
                             this.expectCet,
                             this.failTest);
    });

    it('should update description', function () {
      var cet = new this.CollectionEventType(this.jsonCet);

      this.updateEntity.call(this,
                             cet,
                             'updateDescription',
                             undefined,
                             this.uri('description', cet.id),
                             { studyId: cet.studyId },
                             this.jsonCet,
                             this.expectCet,
                             this.failTest);

      this.updateEntity.call(this,
                             cet,
                             'updateDescription',
                             cet.description,
                             this.uri('description', cet.id),
                             { description: cet.description, studyId: cet.studyId },
                             this.jsonCet,
                             this.expectCet,
                             this.failTest);
    });

    it('should update recurring', function () {
      var cet = new this.CollectionEventType(this.jsonCet);
      this.updateEntity.call(this,
                             cet,
                             'updateRecurring',
                             cet.recurring,
                             this.uri('recurring', cet.id),
                             { recurring: cet.recurring, studyId: cet.studyId },
                             this.jsonCet,
                             this.expectCet,
                             this.failTest);
    });

    describe('for specimen specs', function() {

      beforeEach(function() {
        this.jsonSpec = this.factory.collectionSpecimenSpec();
        this.jsonCet  = this.factory.collectionEventType({ specimenSpecs: [ this.jsonSpec ]});
        this.cet      = new this.CollectionEventType(this.jsonCet);
      });

      it('should add a specimen spec', function () {
        this.updateEntity.call(this,
                               this.cet,
                               'addSpecimenSpec',
                               _.omit(this.jsonSpec, 'uniqueId'),
                               this.uri('spcspec', this.cet.id),
                               _.extend(_.omit(this.jsonSpec, 'uniqueId'), { studyId: this.cet.studyId }),
                               this.jsonCet,
                               this.expectCet,
                               this.failTest);
      });

      it('should update a specimen spec', function () {
        this.updateEntity.call(this,
                               this.cet,
                               'updateSpecimenSpec',
                               this.jsonSpec,
                               sprintf('%s/%s',
                                               this.uri('spcspec', this.cet.id),
                                               this.jsonSpec.uniqueId),
                               _.extend(this.jsonSpec, { studyId: this.cet.studyId }),
                               this.jsonCet,
                               this.expectCet,
                               this.failTest);
      });

      it('should remove a specimen spec', function () {
        var url = sprintf('%s/%s/%d/%s',
                                  this.uri('spcspec', this.cet.studyId),
                                  this.cet.id,
                                  this.cet.version,
                                  this.jsonSpec.uniqueId);

        this.$httpBackend.whenDELETE(url).respond(this.reply(true));
        this.cet.removeSpecimenSpec(this.jsonSpec).then(this.expectCet).catch(this.failTest);
        this.$httpBackend.flush();
      });

      it('throws an error when attempting to remove an invalid specimen spec', function () {
        var self = this;

        self.cet.specimenSpecs = [];
        expect(function () {
          self.cet.removeSpecimenSpec(self.jsonSpec).then(this.expectCet).catch(this.failTest);
        }).toThrowError(/specimen spec with ID not present/);
      });

    });

    describe('for annotation types', function() {

      beforeEach(function() {
        this.jsonAnnotType = this.factory.annotationType();
        this.jsonCet       = this.factory.collectionEventType({ annotationTypes: [ this.jsonAnnotType ]});
        this.cet           = new this.CollectionEventType(this.jsonCet);
      });

      it('should add an annotation type', function () {
        this.updateEntity.call(this,
                               this.cet,
                               'addAnnotationType',
                               _.omit(this.jsonAnnotType, 'uniqueId'),
                               this.uri('annottype', this.cet.id),
                               _.extend(_.omit(this.jsonAnnotType, 'uniqueId'),
                                        { studyId: this.cet.studyId }),
                               this.jsonCet,
                               this.expectCet,
                               this.failTest);
      });

      it('should update an annotation type', function () {
        this.updateEntity.call(this,
                               this.cet,
                               'updateAnnotationType',
                               this.jsonAnnotType,
                               sprintf('%s/%s',
                                               this.uri('annottype', this.cet.id),
                                               this.jsonAnnotType.uniqueId),
                               _.extend(this.jsonAnnotType, { studyId: this.cet.studyId }),
                               this.jsonCet,
                               this.expectCet,
                               this.failTest);
      });

      describe('removing an annotation type', function() {

        it('should remove an annotation type', function () {
          var url = sprintf('%s/%s/%d/%s',
                            this.uri('annottype', this.cet.studyId),
                            this.cet.id,
                            this.cet.version,
                            this.jsonAnnotType.uniqueId);

          this.$httpBackend.whenDELETE(url).respond(this.reply(true));
          this.cet.removeAnnotationType(this.jsonAnnotType).then(this.expectCet).catch(this.failTest);
          this.$httpBackend.flush();
        });

        it('fails when removing an invalid annotation type', function() {
          var jsonAnnotType = _.extend({}, this.jsonAnnotType, { uniqueId: this.factory.stringNext() });
          this.cet.removeAnnotationType(jsonAnnotType)
            .catch(function (err) {
              expect(err).toStartWith('annotation type with ID not present:');
            });
          this.$rootScope.$digest();
        });

      });

    });

    it('inUse has valid URL and returns FALSE', function() {
      var cet     = new this.CollectionEventType(this.factory.collectionEventType());
      this.$httpBackend.whenGET(this.uri('inuse', cet.id)).respond(this.reply(false));
      cet.inUse()
        .then(function (reply) {
          expect(reply).toBe(false);
        })
        .catch(this.failTest);
      this.$httpBackend.flush();
    });

    it('inUse has valid URL and returns TRUE', function() {
      var cet     = new this.CollectionEventType(this.factory.collectionEventType());
      this.$httpBackend.whenGET(this.uri('inuse', cet.id)).respond(this.reply(true));
      cet.inUse()
        .then(function (reply) {
          expect(reply).toBe(true);
        })
        .catch(this.failTest);
      this.$httpBackend.flush();
    });

  });

});
