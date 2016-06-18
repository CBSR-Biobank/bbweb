/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('ceventTypesAddAndSelectDirective', function() {

    var createController = function () {
      this.element = angular.element([
        '<cevent-types-add-and-select',
        '   study="vm.study">',
        '</cevent-types-add-and-select>'
      ].join(''));
      this.scope = this.$rootScope.$new();
      this.scope.vm = { study: this.study };

      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('ceventTypesAddAndSelect');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin, testUtils) {
      var self = this, jsonStudy, jsonCet;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'Study',
                              'CollectionEventType',
                              'factory');

      jsonStudy = self.factory.study();
      jsonCet   = self.factory.collectionEventType(jsonStudy);

      self.study = new self.Study(jsonStudy);
      self.collectionEventType = new self.CollectionEventType(jsonCet);
      self.createController = createController;

      spyOn(self.CollectionEventType, 'list').and.returnValue(self.$q.when([ self.collectionEventType ]));
      spyOn(this.$state, 'go').and.callFake(function () {});

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/collection/ceventTypesAddAndSelect/ceventTypesAddAndSelect.html');
    }));

    it('has valid scope', function() {
      createController.call(this);
      expect(this.controller.study).toBe(this.study);
    });

    it('function add switches to correct state', function() {
      createController.call(this);
      this.controller.add();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.admin.studies.study.collection.ceventTypeAdd');
    });

    it('function select switches to correct state', function() {
      createController.call(this);
      this.controller.select(this.collectionEventType);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.admin.studies.study.collection.ceventType',
                                                  { ceventTypeId: this.collectionEventType.id });
    });

    it('function recurring returns a valid result', function() {
      createController.call(this);
      this.collectionEventType.recurring = false;
      expect(this.controller.getRecurringLabel(this.collectionEventType)).toBe('Not recurring');
      this.collectionEventType.recurring = true;
      expect(this.controller.getRecurringLabel(this.collectionEventType)).toBe('Recurring');
    });

  });

});
