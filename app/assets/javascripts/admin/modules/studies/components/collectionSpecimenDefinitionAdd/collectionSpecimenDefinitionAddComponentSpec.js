/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'

describe('Component: collectionSpecimenDefinitionAdd', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'notificationsService',
                              'domainNotificationService',
                              'Study',
                              'CollectionEventType',
                              'CollectionSpecimenDefinition',
                              'Factory');

      this.jsonCevenType       = this.Factory.collectionEventType();
      this.jsonStudy           = this.Factory.defaultStudy();
      this.collectionEventType = new this.CollectionEventType(this.jsonCevenType);
      this.study               = new this.Study(this.jsonStudy);

      spyOn(this.$state, 'go').and.returnValue(null);
      this.createController = () => {
        this.createControllerInternal(
          [
            '<collection-specimen-definition-add',
            ' study="vm.study"',
            ' collection-event-type="vm.collectionEventType">',
            '</collection-specimen-definition-add>'
          ].join(''),
          {
            study: this.study,
            collectionEventType: this.collectionEventType
          },
          'collectionSpecimenDefinitionAdd');
      };
    });
  });

  it('has valid scope', function() {
    this.createController();

    expect(this.controller.study).toBe(this.study);
    expect(this.controller.collectionEventType).toBe(this.collectionEventType);

    expect(this.controller.submit).toBeFunction();
    expect(this.controller.cancel).toBeFunction();

    expect(this.controller.anatomicalSourceTypes).toBeDefined();
    expect(this.controller.preservTypes).toBeDefined();
    expect(this.controller.preservTempTypes).toBeDefined();
    expect(this.controller.specimenTypes).toBeDefined();
  });

  describe('on submit', function() {

    beforeEach(function() {
      this.jsonSpec            = this.Factory.collectionSpecimenDefinition();
      this.specimenDefinition = new this.CollectionSpecimenDefinition(this.jsonSpec);
    });


    it('can submit a specimen spec', function() {
      spyOn(this.CollectionEventType.prototype, 'addSpecimenDefinition')
        .and.returnValue(this.$q.when(this.collectionEventType));
      spyOn(this.notificationsService, 'submitSuccess').and.callThrough();

      this.createController();
      this.controller.submit(this.specimenDefinition);
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection.ceventType', {}, { reload: true });
      expect(this.notificationsService.submitSuccess).toHaveBeenCalled();
    });

    it('displays an error when submit fails', function() {
      this.createController();
      spyOn(this.CollectionEventType.prototype, 'addSpecimenDefinition')
        .and.returnValue(this.$q.reject('simulated error'));
      spyOn(this.domainNotificationService, 'updateErrorModal').and.returnValue(this.$q.when('OK'));
      this.controller.submit(this.specimenDefinition);
      this.scope.$digest();

      expect(this.domainNotificationService.updateErrorModal).toHaveBeenCalled();
    });

  });

  it('on cancel returns to correct state', function() {
    this.createController();
    this.controller.cancel();
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith('home.admin.studies.study.collection.ceventType');
  });

});
