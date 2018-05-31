/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'

describe('Component: ceventTypeAdd', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              'Study',
                              'CollectionEventType',
                              'Factory');

      this.study = new this.Study(this.Factory.study());

      this.createController = (study) => {
        ComponentTestSuiteMixin.createController.call(
          this,
          '<cevent-type-add study="vm.study"><cevent-type-add>',
          { study: study },
          'ceventTypeAdd');
      };
    });
  });

  it('has valid scope when created', function () {
    this.createController(this.study);
    expect(this.controller.ceventType.isNew()).toBe(true);
  });

  it('can submit a collection event type', function() {
    var $q                   = this.$injector.get('$q'),
        notificationsService = this.$injector.get('notificationsService'),
        $state               = this.$injector.get('$state'),
        ceventType;

    ceventType = new this.CollectionEventType(this.Factory.collectionEventType(this.study));
    this.createController(this.study);

    spyOn(this.CollectionEventType.prototype, 'add').and.callFake(function () {
      return $q.when();
    });
    spyOn(notificationsService, 'submitSuccess').and.callFake(function () {});
    spyOn($state, 'go').and.callFake(function () {});

    this.controller.submit(ceventType);
    this.scope.$digest();

    expect(notificationsService.submitSuccess).toHaveBeenCalled();
    expect($state.go).toHaveBeenCalledWith('^', {}, { reload: true });
  });

  it('on submit error, displays an error modal', function() {
    var q                   = this.$injector.get('$q'),
        domainNotificationService = this.$injector.get('domainNotificationService'),
        ceventType;

    ceventType = new this.CollectionEventType(this.Factory.collectionEventType(this.study));
    this.createController(this.study);

    spyOn(this.CollectionEventType.prototype, 'add').and.callFake(function () {
      var deferred = q.defer();
      deferred.reject('simulated error for test');
      return deferred.promise;
    });
    spyOn(domainNotificationService, 'updateErrorModal').and.callFake(function () {});

    this.controller.submit(ceventType);
    this.scope.$digest();

    expect(domainNotificationService.updateErrorModal)
      .toHaveBeenCalledWith('simulated error for test', 'collection event type');
  });

  it('when user presses the cancel button, goes to correct state', function() {
    var state = this.$injector.get('$state');

    spyOn(state, 'go').and.callFake(function () {});
    this.createController(this.study);

    this.controller.cancel();
    this.scope.$digest();
    expect(state.go).toHaveBeenCalledWith('^');
  });

});
