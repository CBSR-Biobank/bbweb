/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../../../../app';

describe('processingTypeAddComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);
      this.injectDependencies('$rootScope',
                              '$httpBackend',
                              'Study',
                              'Factory');

      this.init();

      this.createController = (study) => {
        this.createControllerInternal(
          '<processing-type-add study="vm.study"><processing-type-add>',
          { study: study },
          'processingTypeAdd');
      };
    })
  });

  it('has valid scope', function() {
    const study = new this.Study(this.Factory.study());
    this.createController(study);
    expect(this.controller.breadcrumbs).toBeDefined();
  });

  it('state configuration is valid', function() {
    const plainStudy = this.Factory.study();
    const study = this.Study.create(this.Factory.study());

    this.$httpBackend.expectGET(this.url('studies', study.slug))
      .respond(this.reply(plainStudy));

    this.gotoUrl(`/admin/studies/${study.slug}/processing/add`);
    this.$httpBackend.flush();
    expect(this.$state.current.name).toBe('home.admin.studies.study.processing.addType');
  });

})
