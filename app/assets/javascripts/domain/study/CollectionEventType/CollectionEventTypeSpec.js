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
      sprintf = require('sprintf-js').sprintf;


  describe('CollectionEventType', function() {

    var CollectionEventType;

    function SuiteMixinFactory(EntityTestSuite, ServerReplyMixin) {

      function SuiteMixin() {
        EntityTestSuite.call(this);
        ServerReplyMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(EntityTestSuite.prototype);
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

      /*
       * Returns 3 collection event types, each one with a different missing field.
       */
      SuiteMixin.prototype.getBadCollectionEventTypes = function () {
        var badSpecimenDescription   = _.omit(this.factory.collectionSpecimenDescription(), 'name'),
            badAnnotationType = _.omit(this.factory.annotationType(), 'name'),
            data = [
              {
                cet: _.omit(this.factory.collectionEventType(), 'name'),
                errMsg : 'Missing required property'
              },
              {
                cet: this.factory.collectionEventType({ specimenDescriptions: [ badSpecimenDescription ]}),
                errMsg : 'specimenDescriptions.*Missing required property'
              },
              {
                cet: this.factory.collectionEventType({ annotationTypes: [ badAnnotationType ]}),
                errMsg : 'annotationTypes.*Missing required property'
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

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(EntityTestSuite, ServerReplyMixin, testDomainEntities) {
      var SuiteMixin = new SuiteMixinFactory(EntityTestSuite, ServerReplyMixin);
      _.extend(this, SuiteMixin.prototype);

      this.injectDependencies('$rootScope',
                              '$httpBackend',
                              'Study',
                              'CollectionEventType',
                              'factory',
                              'testUtils');

      this.jsonCet   = this.factory.collectionEventType();
      this.jsonStudy = this.factory.defaultStudy();

      this.testUtils.addCustomMatchers();
      testDomainEntities.extend();
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
      expect(ceventType.specimenDescriptions).toBeArrayOfSize(0);
      expect(ceventType.annotationTypes).toBeArrayOfSize(0);
    });

    it('fails when creating from an invalid json object', function() {
      var badJsonCet = _.omit(this.factory.collectionEventType(this.jsonStudy), 'name');
      expect(function () {
        CollectionEventType.create(badJsonCet);
      }).toThrowError(/invalid collection event type from server/);
    });

    it('fails when creating from a bad json specimen spec', function() {
      var jsonSpec = _.omit(this.factory.collectionSpecimenDescription(), 'name'),
          badJsonCet = _.extend(this.factory.collectionEventType(this.jsonStudy),
                                { specimenDescriptions: [ jsonSpec ] });

      expect(function () {
        CollectionEventType.create(badJsonCet);
      }).toThrowError(/specimenDescriptions.*Missing required property/);
    });

    it('fails when creating from bad json annotation type data', function() {
      var jsonAnnotType = _.omit(this.factory.annotationType(), 'name'),
          badJsonCet = _.extend(this.factory.collectionEventType(this.jsonStudy),
                                { annotationTypes: [ jsonAnnotType ] });

      expect(function () { CollectionEventType.create(badJsonCet); })
        .toThrowError(/annotationTypes.*Missing required property/);
    });

    it('has valid values when creating from server response', function() {
      var jsonCet = this.factory.collectionEventType(this.jsonStudy),
          ceventType = CollectionEventType.create(jsonCet);
      ceventType.compareToJsonEntity(jsonCet);
    });

    it('can retrieve a collection event type', function() {
      var url = sprintf('%s/%s/%s', this.uri(), this.jsonStudy.id, this.jsonCet.id);

      this.$httpBackend.whenGET(url).respond(this.reply(this.jsonCet));
      CollectionEventType.get(this.jsonStudy.id, this.jsonCet.id)
        .then(this.expectCet).catch(this.failTest);
      this.$httpBackend.flush();
    });

    it('fails when getting a collection event type and it has a bad format', function() {
      var self = this,
          data = self.getBadCollectionEventTypes();

      _.each(data, function (badCet) {
        var url = sprintf('%s/%s/%s', self.uri(), self.jsonStudy.id, badCet.cet.id);

        self.$httpBackend.whenGET(url).respond(self.reply(badCet.cet));
        CollectionEventType.get(self.jsonStudy.id, badCet.cet.id)
          .then(getFail).catch(shouldFail);
        self.$httpBackend.flush();

        function shouldFail(error) {
          expect(error.message).toMatch(badCet.errMsg);
        }
      });

      function getFail() {
        fail('function should not be called');
      }
    });

    it('can list collection event types', function() {
      var url = sprintf('%s/%s', this.uri(), this.jsonStudy.id),
          reply = this.factory.pagedResult([ this.jsonCet ]);

      this.$httpBackend.whenGET(url).respond(this.reply(reply));
      CollectionEventType.list(this.jsonStudy.id)
        .then(expectPagedResult)
        .catch(this.failTest);
      this.$httpBackend.flush();

      function expectPagedResult(pagedResult) {
        expect(pagedResult.items).toBeArrayOfSize(1);
        expect(pagedResult.items[0]).toEqual(jasmine.any(CollectionEventType));
      }
    });

    it('fails when listing collection event types and they have a bad format', function() {
      // assigns result of self.$httpBackend.whenGET() to variable so that the response
      // can be changed inside the loop
      var self = this,
          data = self.getBadCollectionEventTypes(),
          url = sprintf('%s/%s', self.uri(), self.jsonStudy.id),
          reqHandler = self.$httpBackend.whenGET(url);

      data.forEach(function (item) {
        reqHandler.respond(self.reply(self.factory.pagedResult([ item.cet ])));
        CollectionEventType.list(self.jsonStudy.id).then(getFail).catch(shouldFail);
        self.$httpBackend.flush();

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
          url = sprintf('%s/%s', this.uri(), this.jsonStudy.id);

      this.$httpBackend.expectPOST(url).respond(this.reply(this.jsonCet));

      ceventType.add().then(this.expectCet).catch(this.failTest);
      this.$httpBackend.flush();
    });

    it('should remove a collection event type', function() {
      var study = this.Study.create(this.jsonStudy),
          ceventType = new CollectionEventType(this.jsonCet, { study: study }),
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
        this.jsonSpec = this.factory.collectionSpecimenDescription();
        this.jsonCet  = this.factory.collectionEventType({ specimenDescriptions: [ this.jsonSpec ] });
        this.cet      = this.CollectionEventType.create(this.jsonCet);
      });

      it('should add a specimen description', function () {
        this.updateEntity.call(this,
                               this.cet,
                               'addSpecimenDescription',
                               _.omit(this.jsonSpec, 'id'),
                               this.uri('spcdesc', this.cet.id),
                               _.extend(_.omit(this.jsonSpec, 'id'), { studyId: this.cet.studyId }),
                               this.jsonCet,
                               this.expectCet,
                               this.failTest);
      });

      it('should remove a specimen description', function () {
        var url = sprintf('%s/%s/%d/%s',
                          this.uri('spcdesc', this.cet.studyId),
                          this.cet.id,
                          this.cet.version,
                          this.jsonSpec.id);

        this.$httpBackend.whenDELETE(url).respond(this.reply(this.jsonCet));
        this.cet.removeSpecimenDescription(this.jsonSpec).then(this.expectCet).catch(this.failTest);
        this.$httpBackend.flush();
      });

      it('throws an error when attempting to remove an invalid specimen spec', function () {
        var self = this;

        self.cet.specimenDescriptions = [];
        expect(function () {
          self.cet.removeSpecimenDescription(self.jsonSpec).then(this.expectCet).catch(this.failTest);
        }).toThrowError(/specimen description with ID not present/);
      });

    });

    describe('for annotation types', function() {

      beforeEach(function() {
        var study = this.Study.create(this.jsonStudy);

        this.jsonAnnotType = this.factory.annotationType();
        this.jsonCet       = this.factory.collectionEventType({ annotationTypes: [ this.jsonAnnotType ]});
        this.cet           = this.CollectionEventType.create(this.jsonCet, { study: study });
      });

      it('should add an annotation type', function () {
        this.updateEntity.call(this,
                               this.cet,
                               'addAnnotationType',
                               _.omit(this.jsonAnnotType, 'id'),
                               this.uri('annottype', this.cet.id),
                               _.extend(_.omit(this.jsonAnnotType, 'id'),
                                        { studyId: this.cet.studyId }),
                               this.jsonCet,
                               this.expectCet,
                               this.failTest);
      });

      describe('removing an annotation type', function() {

        it('should remove an annotation type', function () {
          var self = this,
              url = sprintf('%s/%s/%d/%s',
                            this.uri('annottype', this.cet.studyId),
                            this.cet.id,
                            this.cet.version,
                            this.jsonAnnotType.id);

          this.$httpBackend.whenDELETE(url).respond(this.reply(this.jsonCet));
          this.cet.removeAnnotationType(this.jsonAnnotType)
            .then(cetCheck)
            .catch(this.failTest);
          this.$httpBackend.flush();

          function cetCheck(ceventType) {
            expect(ceventType).toEqual(jasmine.any(self.CollectionEventType));
          }
        });

        it('fails when removing an invalid annotation type', function() {
          var jsonAnnotType = _.extend({}, this.jsonAnnotType, { id: this.factory.stringNext() });
          this.cet.removeAnnotationType(jsonAnnotType)
            .catch(function (err) {
              expect(err.message).toContain('annotation type with ID not present:');
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
