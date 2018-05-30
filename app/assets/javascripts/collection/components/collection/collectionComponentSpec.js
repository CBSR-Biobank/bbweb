/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { TestSuiteMixin } from 'test/mixins/TestSuiteMixin';
import ngModule from '../../index'

describe('Component: collection', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, TestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Study',
                              'Factory');

      this.createController = () => {
        this.element = angular.element('<collection></collection>');
        this.scope = this.$rootScope.$new();
        this.$compile(this.element)(this.scope);
        this.scope.$digest();
        this.controller = this.element.controller('collection');
      };
    });
  });

  it('has valid scope', function() {
    spyOn(this.Study, 'collectionStudies').and.returnValue(this.$q.when([]));
    this.createController();
    expect(this.controller.isCollectionAllowed).toBe(false);
    expect(this.controller.updateEnabledStudies).toBeFunction();
  });

  it('has valid scope when collections are allowed', function() {
    var study = this.Factory.study();
    spyOn(this.Study, 'collectionStudies')
      .and.returnValue(this.$q.when([study]));
    this.createController();
    expect(this.controller.isCollectionAllowed).toBe(true);
    expect(this.controller.updateEnabledStudies).toBeFunction();
  });

  it('studies are reloaded', function() {
    var callsCount;

    this.Study.collectionStudies = jasmine.createSpy()
      .and.returnValue(this.$q.when([]));

    this.createController();
    callsCount = this.Study.collectionStudies.calls.count();

    this.controller.updateEnabledStudies();
    this.scope.$digest();
    expect(this.Study.collectionStudies.calls.count()).toBe(callsCount + 1);
  });

});
