/**
 * AngularJS Constants used for defining specimen types.
 *
 * @namespace domain.access.RoleIds
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * The {@link domain.access.Role Role} a {@link domain.users.User User} can have.
 *
 * @enum {string}
 * @memberOf domain.access.RoleIds
 */
const RoleIds = {
  WebsiteAdministrator:  'website-administrator',
  UserAdministrator:     'user-administrator',
  SpecimenCollector:     'specimen-collector',
  SpecimenProcessor:     'specimen-processor',
  StudyAdministrator:    'study-administrator',
  StudyUser:             'study-user',
  CentreAdministrator:   'centre-administrator',
  CentreUser:            'centre-user',
  ShippingAdministrator: 'shipping-administrator',
  ShippingUser:          'shipping-user'
};

export default ngModule => ngModule.constant('RoleIds', RoleIds)
