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

describe('ProcessingType', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, EntityTestSuiteMixin, ServerReplyMixin);

      this.injectDependencies('$rootScope',
                              '$httpBackend',
                              'Study',
                              'ProcessingType',
                              'Factory');

      this.url = (...paths) => {
        const args = [ 'studies/proctypes' ].concat(paths);
        return EntityTestSuiteMixin.url.apply(null, args);
      };

      this.createEntities = (options = {}) => {
        const serverAnnotationType = this.Factory.annotationType();
        const serverPt = this.Factory.processingType({
          annotationTypes: [ serverAnnotationType ]
        });
        const processingType = (options.noPtId) ?
              new this.ProcessingType(_.omit(serverPt, 'id')) :  new this.ProcessingType(serverPt);

        return {
          serverStudy:          this.Factory.defaultStudy(),
          serverAnnotationType: serverAnnotationType,
          serverPt:             serverPt,
          processingType:       processingType
        };
      };

      this.getBadProcessingTypeData = () => {
        const badInput = _.omit(this.Factory.inputSpecimenProcessing(), 'count');
        const badSpecimenDefinition = _.omit(this.Factory.processedSpecimenDefinition(), 'name');
        const badAnnotationType = _.omit(this.Factory.annotationType(), 'name');
        const processingType = this.Factory.processingType();

        return [
          {
            pt: 1,
            errRegex : /invalid processing type from server/
          },
          {
            pt: _.omit(this.Factory.processingType(), 'name'),
            errRegex : /Missing required property/
          },
          {
            pt: Object.assign(
              {},
              processingType,
              {
                specimenProcessing: {
                  input: badInput,
                  output: processingType.specimenProcessing.output
                }
              }
            ),
            errRegex: /specimenProcessing.*input.*Missing required property/
          },
          {
            pt: Object.assign(
              {},
              processingType,
              {
                specimenProcessing: {
                  input: processingType.specimenProcessing.input,
                  output: Object.assign({},
                                        processingType.specimenProcessing.output,
                                        { specimenDefinition: badSpecimenDefinition })
                }
              }
            ),
            errRegex : /specimenProcessing.*output.*Missing required property/
          },
          {
            pt: Object.assign(
              {},
              processingType,
              { annotationTypes: [ badAnnotationType ] }
            ),
            errRegex : /annotationTypes.*Missing required property/
          }
        ];
      };

      // used by promise tests
      this.expectPt = (entity) => {
        expect(entity).toEqual(jasmine.any(this.ProcessingType));
      };

      // used by promise tests
      this.failTest = (error) => {
        expect(error).toBeUndefined();
      };

      this.promiseFail = () => {
        fail('function should not be called');
      }
    });
  });

  describe('for constructor', function() {

    it('constructor with no parameters has default values', function() {
      const processingType = new this.ProcessingType();

      expect(processingType.isNew()).toBe(true);
      expect(processingType.studyId).toBe(null);
      expect(processingType.name).toBe('');
      expect(processingType.description).toBe(null);
      expect(processingType.enabled).toBe(false);
    });

    it('constructor with plain object has valid values', function() {
      const f = this.createEntities();
      const processingType = new this.ProcessingType(f.serverPt);

      expect(processingType.isNew()).toBe(false);
      expect(processingType.studyId).toBe(f.serverPt.studyId);
      expect(processingType.name).toBe(f.serverPt.name);
      expect(processingType.description).toBe(f.serverPt.description);
      expect(processingType.enabled).toBe(f.serverPt.enabled);
    });

  });

  it('isNew should be true for a processing type with no ID', function() {
    const f = this.createEntities({ noPtId: true });
    expect(f.processingType.isNew()).toBe(true);
  });

  it('isNew should be false for a processing type that has an ID', function() {
    const f = this.createEntities();
    expect(f.processingType.isNew()).toBe(false);
  });

  describe('when creating', function() {

    it('can create from valid object', function() {
      const f = this.createEntities();
      const processingType = this.ProcessingType.create(f.serverPt);

      expect(processingType.isNew()).toBe(false);
      expect(processingType.studyId).toBe(f.serverPt.studyId);
      expect(processingType.name).toBe(f.serverPt.name);
      expect(processingType.description).toBe(f.serverPt.description);
      expect(processingType.enabled).toBe(f.serverPt.enabled);
    });

    it('fails when creating from a bad data', function() {
      this.getBadProcessingTypeData().forEach(badData => {
        expect(
          () => {
            //console.info(badData.errRegex);
            this.ProcessingType.create(badData.pt);
          }
        ).toThrowError(badData.errRegex);
      });
    });
  });

  describe('when getting from server', function() {

    it('can create from valid object', function() {
      const f = this.createEntities();
      const url = this.url(f.serverStudy.slug, f.serverPt.slug);

      this.$httpBackend.whenGET(url).respond(this.reply(f.serverPt));
      this.ProcessingType.get(f.serverStudy.slug, f.serverPt.slug)
        .then(this.expectPt)
        .catch(this.failTest);
      this.$httpBackend.flush();
    });

    it('fails when server returns bad data', function() {
      const serverStudy = this.Factory.defaultStudy();
      const badData = this.getBadProcessingTypeData();

      badData.forEach(badInfo => {
        const url = this.url(serverStudy.slug, badInfo.pt.slug);

        this.$httpBackend.expectGET(url).respond(this.reply(badInfo.pt));
        this.ProcessingType.get(serverStudy.slug, badInfo.pt.slug)
          .then(this.promiseFail)
          .catch(error => {
            expect(error.message).toMatch(badInfo.errRegex);
          });
        this.$httpBackend.flush();
      });

      this.$httpBackend.verifyNoOutstandingExpectation();
      this.$httpBackend.verifyNoOutstandingRequest();
    });
  });

  describe('when list processing types', function() {

    it('can list collection event types', function() {
      const f = this.createEntities();
      const url = this.url(f.serverStudy.slug);
      const reply = this.Factory.pagedResult([ f.serverPt ]);

      this.$httpBackend.expectGET(url).respond(this.reply(reply));
      this.ProcessingType.list(f.serverStudy.slug)
        .then(pagedResult => {
          expect(pagedResult.items).toBeArrayOfSize(1);
          expect(pagedResult.items[0]).toEqual(jasmine.any(this.ProcessingType));
        })
        .catch(this.failTest);
      this.$httpBackend.flush();
    });

    it('fails when server returns bad data', function() {
      const serverStudy = this.Factory.defaultStudy();
      const badData = this.getBadProcessingTypeData();
      const url = this.url(serverStudy.slug);

      badData.forEach(badInfo => {
        this.$httpBackend.expectGET(url).respond(this.reply(this.Factory.pagedResult([ badInfo.pt ])));
        this.ProcessingType.list(serverStudy.slug)
          .then(this.promiseFail)
          .catch(error => {
            expect(error.message).toMatch(badInfo.errRegex);
          });
        this.$httpBackend.flush();
      });

      this.$httpBackend.verifyNoOutstandingExpectation();
      this.$httpBackend.verifyNoOutstandingRequest();
    });
  });

  it('can add a processing type', function() {
    const f = this.createEntities();

    this.$httpBackend.expectPOST(this.url(f.serverStudy.id))
      .respond(this.reply(f.serverPt));

    f.processingType.add()
      .then(this.expectPt)
      .catch(this.failTest);
    this.$httpBackend.flush();
  });

  it('can update the name on a processing type', function() {
    const f = this.createEntities();

    this.updateEntity(f.processingType,
                      'updateName',
                      f.processingType.name,
                      this.url('update', f.serverStudy.id, f.processingType.id),
                      { property: 'name', newValue: f.processingType.name },
                      f.serverPt,
                      this.expectPt.bind(this),
                      this.failTest.bind(this));
  });

  describe('for the description', function() {

    it('can update the description on a processing type', function() {
      const f = this.createEntities();

      this.updateEntity(f.processingType,
                        'updateDescription',
                        f.processingType.description,
                        this.url('update', f.serverStudy.id, f.processingType.id),
                        { property: 'description', newValue: f.processingType.description },
                        f.serverPt,
                        this.expectPt.bind(this),
                        this.failTest.bind(this));

    });

    it('can update the description on a processing type to be an empty value', function() {
      const f = this.createEntities();
      this.updateEntity(f.processingType,
                        'updateDescription',
                        undefined,
                        this.url('update', f.serverStudy.id, f.processingType.id),
                        { property: 'description', newValue: '' },
                        f.serverPt,
                        this.expectPt.bind(this),
                        this.failTest.bind(this));
    });

  });

  it('can update the enabled attribute on a processing type', function() {
    const f = this.createEntities();

    this.updateEntity(f.processingType,
                      'updateEnabled',
                      f.processingType.enabled,
                      this.url('update', f.serverStudy.id, f.processingType.id),
                      { property: 'enabled', newValue: f.processingType.enabled },
                      f.serverPt,
                      this.expectPt.bind(this),
                      this.failTest.bind(this));
  });

  it('can update the input specimen information', function() {
    const f = this.createEntities();

    this.updateEntity(f.processingType,
                      'updateInputSpecimenDefinition',
                      f.processingType.specimenProcessing.input,
                      this.url('update', f.serverStudy.id, f.processingType.id),
                      {
                        property: 'inputSpecimenDefinition',
                        newValue: f.processingType.specimenProcessing.input
                      },
                      f.serverPt,
                      this.expectPt.bind(this),
                      this.failTest.bind(this));
  });

  it('can update the output specimen information', function() {
    const f = this.createEntities();

    this.updateEntity(f.processingType,
                      'updateOutputSpecimenDefinition',
                      f.processingType.specimenProcessing.output,
                      this.url('update', f.serverStudy.id, f.processingType.id),
                      {
                        property: 'outputSpecimenDefinition',
                        newValue: f.processingType.specimenProcessing.output
                      },
                      f.serverPt,
                      this.expectPt.bind(this),
                      this.failTest.bind(this));
  });

  describe('for annotation types', function() {

    it('should add an annotation type', function () {
      const f = this.createEntities();
      this.updateEntity(f.processingType,
                        'addAnnotationType',
                        _.omit(f.serverAnnotationType, 'id'),
                        this.url('annottype', f.processingType.id),
                        Object.assign(_.omit(f.serverAnnotationType, 'id')),
                        f.serverPt,
                      this.expectPt.bind(this),
                      this.failTest.bind(this));
    });

    describe('removing an annotation type', function() {

      it('should remove an annotation type', function () {
        const f = this.createEntities();
        const url = this.url('annottype',
                             f.processingType.studyId,
                             f.processingType.id,
                             f.processingType.version,
                             f.serverAnnotationType.id);

        this.$httpBackend.whenDELETE(url).respond(this.reply(f.serverPt));
        f.processingType.removeAnnotationType(f.serverAnnotationType)
          .then(this.expectPt.bind(this))
          .catch(this.failTest.bind(this));
        this.$httpBackend.flush();
      });

      it('fails when removing an invalid annotation type', function() {
        const f = this.createEntities();
        var serverAnnotationType = Object.assign({},
                                                 f.serverAnnotationType,
                                                 { id: this.Factory.stringNext() });
        f.processingType.removeAnnotationType(serverAnnotationType)
          .catch((err) => {
            expect(err.message).toContain('annotation type with ID not present:');
          });
        this.$rootScope.$digest();
      });

    });

  });

  it('should remove a processing type', function() {
    const f = this.createEntities();
    this.$httpBackend.expectDELETE(this.url(f.serverStudy.id,
                                            f.processingType.id,
                                            f.processingType.version))
      .respond(this.reply(true));

    f.processingType.remove()
      .then((result) => {
        expect(result).toBeTrue();
      })
      .catch(this.failTest.bind(this));
    this.$httpBackend.flush();
  });

});
