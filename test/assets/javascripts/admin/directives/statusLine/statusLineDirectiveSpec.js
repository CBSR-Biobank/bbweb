/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  function SuiteMixinFactory(TestSuiteMixin) {

    function SuiteMixin() {
    }

    SuiteMixin.prototype = Object.create(TestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    SuiteMixin.prototype.createScope = function (entity, showState) {
      showState = showState || true;
      this.scope = this.$rootScope;

      this.element = angular.element(
        '<status-line item="model.entity" show-state="' + showState.toString() + '"></status-line>');
      this.scope.model = { entity: entity };

      this.$compile(this.element)(this.scope);
      this.scope.$digest();
    };

    return SuiteMixin;
  }


  describe('Directive: statusLineDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin, factory) {
      var self = this;

      var SuiteMixin = new SuiteMixinFactory(TestSuiteMixin);
      _.extend(this, SuiteMixin.prototype);

      self.injectDependencies('$rootScope',
                              '$compile',
                              'Centre',
                              '$filter',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/common/directives/statusLine/statusLine.html');
    }));

    it('has the correct number of table cells', function() {
      var centre = new this.Centre(this.factory.centre()),
          cells;

      this.createScope(centre);
      cells = this.element.find('td');
      expect(cells.length).toBe(3);
      _.each(_.range(3), function (n) {
        var small = cells.eq(n).find('small');
        expect(small.eq(0)).toHaveClass('text-muted');
      });

      expect(cells.eq(0).find('small').eq(0).text()).toContain(centre.state);
      expect(cells.eq(1).find('small').eq(0).text()).toContain(this.$filter('timeago')(centre.timeAdded));
    });

    it('displays correpct value if timeModified has a value', function() {
      var centre = new this.Centre(this.factory.centre()),
          cells;

      this.createScope(centre);
      cells = this.element.find('td');

      expect(centre.timeModified).toBeDefined();
      expect(cells.eq(2).find('small').eq(0).text()).toContain(this.$filter('timeago')(centre.timeModified));
    });

    it('displays correct value if timeModified is undefined', function() {
      var centre = new this.Centre(this.factory.centre()),
          cells;

      centre.timeModified = undefined;
      this.createScope(centre);

      cells = this.element.find('td');
      expect(cells.eq(2).find('small').eq(0).text()).toContain('Never');
    });

    it('does not show status if `showStatus` is false', function() {
      var centre = new this.Centre(this.factory.centre()),
          cells;

      this.createScope(centre, false);
      cells = this.element.find('td');
      expect(cells.eq(0).find('small').eq(0).text()).not.toContain(centre.statusLabel);
    });


  });

});
