/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Component: collection', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin.prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Study',
                              'factory');

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
    spyOn(this.Study, 'collectionStudies').and.returnValue(this.$q.when(this.factory.pagedResult([])));
    this.createController();
    expect(this.controller.isCollectionAllowed).toBe(false);
    expect(this.controller.updateEnabledStudies).toBeFunction();
  });

  it('has valid scope when collections are allowed', function() {
    var study = this.factory.study();
    spyOn(this.Study, 'collectionStudies')
      .and.returnValue(this.$q.when(this.factory.pagedResult([study])));
    this.createController();
    expect(this.controller.isCollectionAllowed).toBe(true);
    expect(this.controller.updateEnabledStudies).toBeFunction();
  });

  it('studies are reloaded', function() {
    var callsCount;

    this.Study.collectionStudies = jasmine.createSpy()
      .and.returnValue(this.$q.when(this.factory.pagedResult([])));

    this.createController();
    callsCount = this.Study.collectionStudies.calls.count();

    this.controller.updateEnabledStudies();
    this.scope.$digest();
    expect(this.Study.collectionStudies.calls.count()).toBe(callsCount + 1);
  });

});
