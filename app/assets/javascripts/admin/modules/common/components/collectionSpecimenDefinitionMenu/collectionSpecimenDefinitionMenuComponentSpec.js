/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import _ from 'lodash';
import ngModule from '../../index';

describe('collectionSpecimenDefinitionMenuComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('Factory');

      this.createController = (specimenDefinition, allowChanges) => {
        this.view = jasmine.createSpy('view');
        this.update = jasmine.createSpy('update');
        this.remove = jasmine.createSpy('remove');

        this.createControllerInternal(
          `<collection-specimen-definition-menu
              specimen-definition="vm.specimenDefinition"
              allow-changes="${allowChanges}"
              on-view="vm.view"
              on-update="vm.update"
              on-remove="vm.remove">
           </collection-specimen-definition-menu>`,
          {
            specimenDefinition,
            view:   this.view,
            update: this.update,
            remove: this.remove
          },
          'collectionSpecimenDefinitionMenu');
      }
    });
  });

  it('initialization is valid', function() {
    const specimenDefinition = this.Factory.collectionSpecimenDefinition();
    this.createController(specimenDefinition, true);

    expect(this.controller.specimenDefinition).toBe(specimenDefinition);
    expect(this.controller.allowChanges).toBe(true);
    expect(this.controller.onView()).toBeFunction();
    expect(this.controller.onUpdate()).toBeFunction();
    expect(this.controller.onRemove()).toBeFunction();
  });

  describe('when allowChages is TRUE', function() {

    it('when allowChages is TRUE only two menu options are available', function() {
      const specimenDefinition = this.Factory.collectionSpecimenDefinition();
      this.createController(specimenDefinition, true);
      const hyperlinks = this.element.find('a');
      expect(hyperlinks.length).toBe(2);
    });

    it('`onView` is called', function() {
      const specimenDefinition = this.Factory.collectionSpecimenDefinition();
      this.createController(specimenDefinition, true);
      const hyperlinks = this.element.find('a');

      let updateHyperlink, removeHyperlink;

      expect(hyperlinks.length).toBe(2);
      _.range(hyperlinks.length).forEach(index => {
        const hyperlinkText = hyperlinks.eq(index).text().trim();
        if (hyperlinkText === 'Update this specimen') {
          updateHyperlink = hyperlinks.eq(index);
        }
        if (hyperlinkText === 'Remove this specimen') {
          removeHyperlink = hyperlinks.eq(index);
        }
      });

      expect(updateHyperlink).toBeDefined();
      expect(removeHyperlink).toBeDefined();

      updateHyperlink.click().trigger('change');
      expect(this.update).toHaveBeenCalled();
      removeHyperlink.click().trigger('change');
      expect(this.remove).toHaveBeenCalled();
    });

  });

  describe('when allowChages is FALSE', function() {

    it('only one menu options is available', function() {
      const specimenDefinition = this.Factory.collectionSpecimenDefinition();
      this.createController(specimenDefinition, false);
      const hyperlinks = this.element.find('a');
      expect(hyperlinks.length).toBe(1);
    });

    it('`onView` is called', function() {
      const specimenDefinition = this.Factory.collectionSpecimenDefinition();
      this.createController(specimenDefinition, false);
      const hyperlinks = this.element.find('a');

      let viewHyperlink;

      expect(hyperlinks.length).toBe(1);
      _.range(hyperlinks.length).forEach(index => {
        const hyperlinkText = hyperlinks.eq(index).text().trim();
        if (hyperlinkText === 'View this specimen') {
          viewHyperlink = hyperlinks.eq(index);
        }
      });

      expect(viewHyperlink).toBeDefined();
      viewHyperlink.click().trigger('change');
      expect(this.view).toHaveBeenCalled();
    });
  });

});
