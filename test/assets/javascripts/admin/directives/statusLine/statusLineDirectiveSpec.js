/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: statusLineDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(jsonEntities, testUtils) {
      var self = this;

      testUtils.putHtmlTemplates(
        '/assets/javascripts/admin/directives/statusLine/statusLine.html');

      self.createScope = setupScope();
      self.jsonEntities = self.$injector.get('jsonEntities');

      //--

      function setupScope() {
        var $rootScope = self.$injector.get('$rootScope'),
            $compile   = self.$injector.get('$compile');

        return create;

        function create(entity) {
          self.scope = $rootScope;

          self.element = angular.element('<status-line item="model.entity"></status-line>');
          self.scope.model = { entity: entity };

          $compile(self.element)(self.scope);
          self.scope.$digest();
        }
      }
    }));

    it('has the correct number of table cells', function() {
      var Centre = this.$injector.get('Centre'),
          $filter = this.$injector.get('$filter'),
          centre = new Centre(this.jsonEntities.centre()),
          cells;

      this.createScope(centre);
      cells = this.element.find('td');
      expect(cells.length).toBe(3);
      _.each(_.range(3), function (n) {
        var small = cells.eq(n).find('small');
        expect(small.eq(0)).toHaveClass('text-muted');
      });

      expect(cells.eq(0).find('small').eq(0).text()).toContain(centre.statusLabel);
      expect(cells.eq(1).find('small').eq(0).text()).toContain($filter('timeago')(centre.timeAdded));
    });

    it('displays correct value if timeModified has a value', function() {
      var Centre = this.$injector.get('Centre'),
          $filter = this.$injector.get('$filter'),
          centre = new Centre(this.jsonEntities.centre()),
          cells;

      this.createScope(centre);
      cells = this.element.find('td');

      expect(centre.timeModified).toBeDefined();
      expect(cells.eq(2).find('small').eq(0).text()).toContain($filter('timeago')(centre.timeModified));
    });

    it('displays correct value if timeModified is undefined', function() {
      var Centre = this.$injector.get('Centre'),
          centre = new Centre(this.jsonEntities.centre()),
          cells;

      centre.timeModified = undefined;
      this.createScope(centre);

      cells = this.element.find('td');
      expect(cells.eq(2).find('small').eq(0).text()).toContain('Never');
    });

  });

});
