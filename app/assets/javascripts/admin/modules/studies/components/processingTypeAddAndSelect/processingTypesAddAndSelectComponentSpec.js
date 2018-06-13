/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import ngModule from '../../index'

describe('processingTypesAddAndSelectComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin, ServerReplyMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              '$httpBackend',
                              'Study',
                              'CollectionEventType',
                              'Factory');

      this.$state.go = jasmine.createSpy().and.returnValue(null);

      this.expectGET = (studySlug, reponseObjects = []) => {
        const url = this.url('studies/proctypes', studySlug) + '?limit=5&page=1';
        this.$httpBackend.expectGET(url)
          .respond(this.reply(this.Factory.pagedResult(reponseObjects)));
      };

      this.createController =
        (jsonStudy = this.Factory.study(), jsonProcessingType = this.Factory.processingType()) => {
      this.study = new this.Study(jsonStudy);

          let responseObjects = [];

          if (jsonProcessingType) {
            this.processingType = new this.CollectionEventType(jsonProcessingType);
            responseObjects = [ jsonProcessingType ];
        }

          this.expectGET(this.study.slug, responseObjects);
        ComponentTestSuiteMixin.createController.call(
          this,
            `<processing-types-add-and-select
              study="vm.study"
              processing-types="vm.collectionEventTypes">
           </processing-types-add-and-select>`,
          {
              study:           this.study,
              processingTypes: [ this.processingType ]
          },
          'processingTypesAddAndSelect');
          this.$httpBackend.flush();
      };
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  it('has valid scope', function() {
    this.createController(this.Factory.study(), null);
     expect(this.controller.study).toBe(this.study);
     expect(this.controller.processingTypes).toBeArrayOfSize(0);
  });

  it('function add switches to correct state', function() {
    this.createController();
    this.controller.add();
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith('home.admin.studies.study.processing.addType.information');
  });

  it('function select switches to correct state', function() {
    this.createController();
    this.controller.select(this.processingType);
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith(
      'home.admin.studies.study.processing.viewType',
      { processingTypeSlug: this.processingType.slug });
  });

  it('function pageChanged switches to correct state', function() {
    this.createController();
    this.controller.pageChanged();
    this.expectGET(this.study.slug, []);
    this.$httpBackend.flush();
    expect(this.$state.go).toHaveBeenCalledWith('home.admin.studies.study.processing');
  });

  describe('for updating name filter', function() {

    it('filter is updated when user enters a value', function() {
      var name = this.Factory.stringNext();
      this.createController();

      const url = this.url('studies/proctypes', this.study.slug) +
            `?filter=name:like:${name}&limit=5&page=1`;
      this.$httpBackend.expectGET(url)
        .respond(this.reply(this.Factory.pagedResult([])));

      this.controller.nameFilter = name;
      this.controller.nameFilterUpdated();
      this.$httpBackend.flush();

      expect(this.controller.pagerOptions.filter).toEqual('name:like:' + name);
      expect(this.controller.pagerOptions.page).toEqual(1);
      expect(this.controller.displayState).toBe(this.controller.displayStates.NO_RESULTS);
    });

    it('filter is updated when user clears the value', function() {
      this.createController();
      this.controller.nameFilter = '';
      this.controller.nameFilterUpdated();
      this.expectGET(this.study.slug, []);
      this.$httpBackend.flush();

      expect(this.controller.pagerOptions.filter).toBeEmptyString();
      expect(this.controller.pagerOptions.page).toEqual(1);
    });

  });

});
