/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { TestSuiteMixin } from 'test/mixins/TestSuiteMixin';

const ngModule = 'FIXME'

xdescribe('StudyViewer', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, TestSuiteMixin);
      this.injectDependencies('StudyViewer',
                              'Study',
                              'EntityViewer',
                              '$filter',
                              'Factory');

      this.centre = this.Factory.centre();
    });
  });

  it('should open a modal when created', function () {
    var modal = this.$injector.get('$uibModal');
    spyOn(modal, 'open').and.returnValue(this.fakeModal());

    // jshint unused:false
    var study = this.Factory.study(),
        viewer = new this.StudyViewer(study);        // eslint-disable-line no-unused-vars


    expect(modal.open).toHaveBeenCalled();
  });

  it('should display valid attributes', function() {
    var attributes = [],
        study = this.Factory.study(),
        viewer;                                 // eslint-disable-line no-unused-vars

    this.EntityViewer.prototype.addAttribute = jasmine.createSpy()
      .and.callFake((label, value) => {
        attributes.push({label: label, value: value});
      });

    viewer = new this.StudyViewer(study);

    expect(attributes).toBeArrayOfSize(3);

    attributes.forEach((attr) => {
      switch (attr.label) {
      case 'Name':
        expect(attr.value).toBe(study.name);
        break;
      case 'Description':
        expect(attr.value).toBe(this.$filter('truncate')(study.description, 60));
        break;
      case 'State':
        expect(attr.value).toBe(study.state.toUpperCase());
        break;
      default:
        jasmine.getEnv().fail('label is invalid: ' + attr.label);
      }
    });
  });

});
